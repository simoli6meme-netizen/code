package org.my.work.code2026.common;

import jakarta.annotation.Resource;
import org.my.work.code2026.mapper.AdminMapper;
import org.my.work.code2026.mapper.DeliveryMapper;
import org.my.work.code2026.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 启动时把历史明文密码升级成 BCrypt 密文。
 *
 * 两个安全设计：
 * 1. 只处理「非 BCrypt 格式」的行，重复执行不会把密文再加密一遍（那样密码就废了）；
 * 2. 整体 try-catch，迁移失败只打日志、不阻塞启动——登录里的渐进式升级还能兜底，
 *    不会出现「迁移脚本挂了，所有人都登不进去」的线上事故。
 *
 * 账号量在演示项目里极小，全表扫一遍开销可以忽略；真实系统应改成游标分批。
 */
@Component
public class PasswordMigrator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrator.class);

    @Resource
    private PasswordHelper passwordHelper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private AdminMapper adminMapper;
    @Resource
    private DeliveryMapper deliveryMapper;

    @Value("${security.password.migrate-on-startup:true}")
    private boolean enabled;

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.info("密码迁移开关已关闭（security.password.migrate-on-startup=false），跳过");
            return;
        }
        int total = 0;
        try {
            total += migrate("user", userMapper.selectPasswordRows(), userMapper::updatePasswordById);
            total += migrate("admin", adminMapper.selectPasswordRows(), adminMapper::updatePasswordById);
            total += migrate("delivery", deliveryMapper.selectPasswordRows(), deliveryMapper::updatePasswordById);
            log.info("密码迁移结束，本次把 {} 条明文密码升级为 BCrypt", total);
        } catch (Exception e) {
            log.warn("密码迁移中断（已升级 {} 条），剩余记录会在用户下次登录时自动升级：{}", total, e.getMessage());
        }
    }

    private int migrate(String table, List<Map<String, Object>> rows, BiConsumer<Integer, String> saver) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Map<String, Object> row : rows) {
            Object idValue = row.get("id");
            Object pwdValue = row.get("password");
            if (idValue == null || pwdValue == null) {
                continue;
            }
            String stored = pwdValue.toString();
            if (passwordHelper.isEncoded(stored)) {
                continue;
            }
            saver.accept(((Number) idValue).intValue(), passwordHelper.encode(stored));
            count++;
        }
        if (count > 0) {
            log.info("表 {} 升级 {} 条明文密码", table, count);
        }
        return count;
    }
}
