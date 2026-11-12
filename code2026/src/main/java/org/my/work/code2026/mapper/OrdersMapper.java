package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
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

    /** 扫描超时且仍未被接单的订单；close_deadline 为 NULL 的历史订单不会被扫到 */
    List<Integer> selectExpiredIds();

    /**
     * 条件更新抢「关单权」：只有状态不是已取消才能关，影响行数 0 说明已被别的线程/实例处理。
     * 定时任务和手工取消共用，保证退款、回补库存最多执行一次。
     */
    int closeIfPending(@Param("id") Integer id);

    /**
     * 骑手接单：条件更新抢单，影响行数 0 表示已被别的骑手接走。
     * 先 select 状态再 update 会让两个并发请求都读到「待接单」并都接单成功。
     */
    @Update("update `orders` set status = '配送中', delivery_id = #{deliveryId} "
            + "where id = #{orderId} and status = '待接单' and delivery_id is null")
    int acceptIfPending(@Param("orderId") Integer orderId, @Param("deliveryId") Integer deliveryId);
}