package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.my.work.code2026.entity.Collect;

import java.util.List;

/**
 * 操作collect相关数据接口
*/
@Mapper
public interface CollectMapper {

    /**
      * 新增
    */
    int insert(Collect collect);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Collect collect);

    /**
      * 根据ID查询
    */
    Collect selectById(Integer id);

    /**
      * 查询所有
    */
    List<Collect> selectAll(Collect collect);

}