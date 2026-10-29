package org.my.work.code2026.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Account;
import org.my.work.code2026.entity.Delivery;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.DeliveryMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DeliveryService {

    @Resource
    private DeliveryMapper deliveryMapper;

    public Delivery login(Account account) {
        // 1. 参数校验
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword())) {
            throw new CustomException("账号和密码不能为空");
        }

        // 2. 按用户名查询
        Delivery delivery = deliveryMapper.selectByUsername(account.getUsername());

        // 3. 用户不存在 或 密码不匹配 → 统一提示（防用户名枚举）
        if (ObjectUtil.isNull(delivery)
                || !Objects.equals(account.getPassword(), delivery.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 如果密码是加密存储，上面这行改为：
        // || !passwordEncoder.matches(account.getPassword(), delivery.getPassword())

        // 4. 可选：判断账号状态（禁用/审核中）
        // if ("0".equals(delivery.getStatus())) throw new CustomException("账号已被禁用");

        return delivery;
    }

    /**
     * 分页查询（支持按姓名/手机号模糊搜索）
     */
    public PageInfo<Delivery> selectPage(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        List<Delivery> list = deliveryMapper.selectAll(name);
        return PageInfo.of(list);
    }

    /**
     * 根据 ID 查询
     */
    public Delivery selectById(Integer id) {
        return deliveryMapper.selectById(id);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        deliveryMapper.deleteById(id);
    }

    /**
     * 新增
     */
    public void add(Delivery delivery) {
        String username = delivery.getUsername();

        // 1. 查重：查到了就拒绝
        Delivery exist = deliveryMapper.selectByUsername(username);
        if (exist != null) {
            throw new CustomException("新增失败，账号重复!");
        }

        // 2. 默认值设在「要插入的 delivery」上，不是 exist 上
        if (StrUtil.isBlank(delivery.getPassword())) {
            delivery.setPassword("123");
        }
        if (StrUtil.isBlank(delivery.getName())) {
            delivery.setName(delivery.getUsername());
        }
        delivery.setRole("外卖员");

        // 3. 插入修正后的 delivery
        deliveryMapper.insert(delivery);
    }

    /**
     * 修改（全字段更新）
     */
    public void update(Delivery delivery) {
        if (delivery.getId() == null) {
            throw new CustomException("缺少主键 ID，无法更新");
        }
        deliveryMapper.updateById(delivery);
    }

    /**
     * 修改密码：校验原密码后写入新密码（与管理员 / 普通用户保持一致）
     */
    public void updatePassword(Account account) {
        Delivery db = deliveryMapper.selectById(account.getId());
        if (ObjectUtil.isNull(db)) {
            throw new CustomException("账号不存在");
        }
        if (!Objects.equals(account.getPassword(), db.getPassword())) {
            throw new CustomException("原密码错误");
        }
        db.setPassword(account.getNewPassword());
        deliveryMapper.updateById(db);
    }
}
