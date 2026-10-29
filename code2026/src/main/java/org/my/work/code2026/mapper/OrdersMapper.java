package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.my.work.code2026.entity.Orders;

import java.util.List;

/**
 * 操作orders相关数据接口
*/
@Mapper
public interface OrdersMapper {

    /**
      * 新增
    */
    int insert(Orders orders);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Orders orders);

    /**
      * 根据ID查询
    */
    Orders selectById(Integer id);

    /**
      * 查询所有
    */
    List<Orders> selectAll(Orders orders);

    /**
      * 按条件统计数量
    */
    int selectCount(Orders orders);

    @Delete("delete from `order_detail` where order_id = #{orderId}")
    void deleteByOrderId(Integer orderId);
}