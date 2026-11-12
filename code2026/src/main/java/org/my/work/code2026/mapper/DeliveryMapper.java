package org.my.work.code2026.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.my.work.code2026.entity.Delivery;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeliveryMapper {
    Delivery selectByUsername(String username);

    void insert(Delivery delivery);

    List<Delivery> selectAll(String name);

    Delivery selectById(Integer id);

    void deleteById(Integer id);

    void updateById(Delivery delivery);

    /** 供启动时的密码迁移使用 */
    @Select("select id, password from `delivery` where password is not null and password <> ''")
    List<Map<String, Object>> selectPasswordRows();

    @Update("update `delivery` set password = #{password} where id = #{id}")
    int updatePasswordById(@Param("id") Integer id, @Param("password") String password);
}
