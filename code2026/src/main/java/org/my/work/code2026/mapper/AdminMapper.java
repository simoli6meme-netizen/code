package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.my.work.code2026.entity.Admin;

import java.util.List;
import java.util.Map;

/**
 * 操作admin相关数据接口
*/
@Mapper
public interface AdminMapper {

    /**
      * 新增
    */
    int insert(Admin admin);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Admin admin);

    /**
      * 根据ID查询
    */
    Admin selectById(Integer id);

    /**
      * 查询所有
    */
    List<Admin> selectAll(Admin admin);

    @Select("select * from `admin` where username = #{username}")
    Admin selectByUsername(String username);

    /** 供启动时的密码迁移使用 */
    @Select("select id, password from `admin` where password is not null and password <> ''")
    List<Map<String, Object>> selectPasswordRows();

    @Update("update `admin` set password = #{password} where id = #{id}")
    int updatePasswordById(@Param("id") Integer id, @Param("password") String password);

}