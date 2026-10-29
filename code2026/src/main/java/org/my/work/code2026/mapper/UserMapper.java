package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.my.work.code2026.entity.User;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> selectAll(String name);

    void deleteById(Integer id);

    void insert(User user);

    User selectByUsername(String username);

    void updateById(User user);

    User selectById(Integer id);
}
