package org.my.work.code2026.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.PasswordHelper;
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

    @Resource
    private PasswordHelper passwordHelper;

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
        // 统一落库为 BCrypt 密文（含上面补的默认密码）
        user.setPassword(passwordHelper.encode(user.getPassword()));
        user.setRole("普通用户");
        user.setAccount(BigDecimal.ZERO);
        userMapper.insert(user);
    }

    public void update(User user) {
        // 传了密码才重新加密；没传就置空让 XML 跳过该字段，
        // 否则全字段更新会把库里的密文覆盖成 NULL，账号直接登不上
        if (StrUtil.isNotBlank(user.getPassword())) {
            user.setPassword(passwordHelper.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userMapper.updateById(user);
    }

    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!passwordHelper.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 渐进式升级：命中历史明文说明这条还没迁移，登录成功后立刻改写成密文
        if (passwordHelper.needsUpgrade(dbUser.getPassword())) {
            userMapper.updatePasswordById(dbUser.getId(), passwordHelper.encode(account.getPassword()));
        }
        dbUser.setPassword(null);   // 内存对象里也不留密码，避免后续被误序列化
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
        if (!passwordHelper.matches(account.getPassword(), dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(passwordHelper.encode(account.getNewPassword()));
        userMapper.updateById(dbUser);
    }


}
