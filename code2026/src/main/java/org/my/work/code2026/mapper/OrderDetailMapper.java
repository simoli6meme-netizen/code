package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.my.work.code2026.entity.OrderDetail;

import java.util.List;

/**
 * 操作orderDetail相关数据接口
*/
@Mapper
public interface OrderDetailMapper {

    /**
      * 新增
    */
    int insert(OrderDetail orderDetail);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(OrderDetail orderDetail);

    /**
      * 根据ID查询
    */
    OrderDetail selectById(Integer id);

    /**
      * 查询所有
    */
    List<OrderDetail> selectAll(OrderDetail orderDetail);

}