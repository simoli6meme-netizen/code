package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.my.work.code2026.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {
    List<User> selectAll(String name);

    void deleteById(Integer id);

    void insert(User user);

    User selectByUsername(String username);

    void updateById(User user);

    User selectById(Integer id);

    /**
     * 原子扣减余额：一条 SQL 完成「判断够不够 + 扣减」，避免先查后改的超扣问题。
     * 返回 0 表示余额不足。
     */
    @Update("update `user` set account = account - #{amount} where id = #{id} and account >= #{amount}")
    int deductAccount(@Param("id") Integer id, @Param("amount") BigDecimal amount);

    /**
     * 原子增加余额：充值、取消退款、超时关单都走这里。
     * 读出来 add 完再 updateById 会在并发时互相覆盖，导致少退/少充。
     */
    @Update("update `user` set account = account + #{amount} where id = #{id}")
    int restoreAccount(@Param("id") Integer id, @Param("amount") BigDecimal amount);

    /** 供启动时的密码迁移使用：只取 id 和 password，避免全表字段往返 */
    @Select("select id, password from `user` where password is not null and password <> ''")
    List<Map<String, Object>> selectPasswordRows();

    @Update("update `user` set password = #{password} where id = #{id}")
    int updatePasswordById(@Param("id") Integer id, @Param("password") String password);
}
