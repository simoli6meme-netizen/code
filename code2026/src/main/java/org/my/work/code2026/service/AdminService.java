package org.my.work.code2026.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.PasswordHelper;
import org.my.work.code2026.entity.Account;
import org.my.work.code2026.entity.Admin;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.AdminMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private PasswordHelper passwordHelper;

    /**
     * 新增
     */
    public void add(Admin admin) {
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        if (ObjectUtil.isEmpty(admin.getPassword())) {
            admin.setPassword("admin");
        }
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        // 统一落库为 BCrypt 密文（含上面补的默认密码）
        admin.setPassword(passwordHelper.encode(admin.getPassword()));
        admin.setRole("管理员");
        adminMapper.insert(admin);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        adminMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Admin admin) {
        // 同 UserService.update：没传密码时不覆盖已有密文
        if (StrUtil.isNotBlank(admin.getPassword())) {
            admin.setPassword(passwordHelper.encode(admin.getPassword()));
        } else {
            admin.setPassword(null);
        }
        adminMapper.updateById(admin);
    }

    /**
     * 根据ID查询
     */
    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Admin> selectAll(Admin admin) {
        return adminMapper.selectAll(admin);
    }

    /**
     * 分页查询
     */
    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> list = adminMapper.selectAll(admin);
        return PageInfo.of(list);
    }

    /**
     * 登录
     */
    public Account login(Account account) {
        Account dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        if (!passwordHelper.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 渐进式升级：命中历史明文说明这条还没迁移，登录成功后立刻改写成密文
        if (passwordHelper.needsUpgrade(dbAdmin.getPassword())) {
            adminMapper.updatePasswordById(dbAdmin.getId(), passwordHelper.encode(account.getPassword()));
        }
        dbAdmin.setPassword(null);
        return dbAdmin;
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        if (!passwordHelper.matches(account.getPassword(), dbAdmin.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbAdmin.setPassword(passwordHelper.encode(account.getNewPassword()));
        adminMapper.updateById(dbAdmin);
    }

}