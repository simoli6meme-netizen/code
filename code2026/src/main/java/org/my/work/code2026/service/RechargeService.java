package org.my.work.code2026.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Account;
import org.my.work.code2026.entity.Recharge;
import org.my.work.code2026.entity.User;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.RechargeMapper;
import org.my.work.code2026.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class RechargeService {

    @Resource
    private RechargeMapper rechargeMapper;

    @Resource
    private UserMapper userMapper;
    /**
     * 新增
     */
    @Transactional
    public void add(Recharge recharge) {
        Integer userId = recharge.getUserId();
        User user=userMapper.selectById(userId);
        user.setAccount(user.getAccount().add(recharge.getMoney()));
        userMapper.updateById(user);
        recharge.setTime(DateUtil.now());
        rechargeMapper.insert(recharge);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        rechargeMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Recharge recharge) {
        rechargeMapper.updateById(recharge);
    }

    /**
     * 根据ID查询
     */
    public Recharge selectById(Integer id) {
        return rechargeMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Recharge> selectAll(Recharge recharge) {
        return rechargeMapper.selectAll(recharge);
    }

    /**
     * 分页查询
     */
    public PageInfo<Recharge> selectPage(Recharge recharge, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Recharge> list = rechargeMapper.selectAll(recharge);
        return PageInfo.of(list);
    }


}