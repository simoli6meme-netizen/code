package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.my.work.code2026.entity.Carousel;

import java.util.List;

/**
 * 操作carousel相关数据接口
*/
@Mapper
public interface CarouselMapper {

    /**
      * 新增
    */
    int insert(Carousel carousel);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Carousel carousel);

    /**
      * 根据ID查询
    */
    Carousel selectById(Integer id);

    /**
      * 查询所有
    */
    List<Carousel> selectAll(Carousel carousel);

}