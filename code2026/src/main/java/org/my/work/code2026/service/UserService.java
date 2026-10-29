package org.my.work.code2026.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Account;
import org.my.work.code2026.entity.Admin;
import org.my.work.code2026.entity.User;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public List<User> selectByAll(String name) {
        return userMapper.selectAll(name);
    }

    public PageInfo<User> selectPage(Integer pageNun,Integer pageSize,String name){
        PageHelper.startPage(pageNun,pageSize);
        List<User> list=userMapper.selectAll(name);
        return PageInfo.of(list);
    }

    public void deleteById(Integer id){
        userMapper.deleteById(id);
    }

    public void add(User user) {
        String username=user.getUsername();
        User dbUser = userMapper.selectByUsername(username);
        if(dbUser != null){
            throw new CustomException("新增失败，账号重复!");
        }
        if(StrUtil.isBlank(user.getPassword())){
            user.setPassword("123");
        }
        if(StrUtil.isBlank(user.getName())){
            user.setName(user.getUsername());
        }
        user.setRole("普通用户");
        user.setAccount(BigDecimal.ZERO);
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbUser;
    }

    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }


}
