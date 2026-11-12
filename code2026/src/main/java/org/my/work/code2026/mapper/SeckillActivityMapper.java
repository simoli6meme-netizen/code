package org.my.work.code2026.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.my.work.code2026.entity.SeckillActivity;

import java.util.List;

/**
 * 秒杀活动数据访问
 **/
@Mapper
public interface SeckillActivityMapper {

    int insert(SeckillActivity activity);

    SeckillActivity selectById(Integer id);

    List<SeckillActivity> selectAll();

    /**
     * 数据库库存 -1。
     * 带 stock > 0 条件，返回 0 说明库里也没货了，是 Redis 之外的最终防线。
     */
    int deductStock(Integer id);

    /**
     * 写入秒杀成功记录。
     * uk_activity_user 唯一索引冲突时抛 DuplicateKeyException，用于兜底「一人一单」。
     */
    int insertRecord(@Param("activityId") Integer activityId, @Param("userId") Integer userId);

    /** 订单创建成功后回填订单ID */
    int updateRecordOrderId(@Param("activityId") Integer activityId,
                            @Param("userId") Integer userId,
                            @Param("orderId") Integer orderId);

    /** 某场活动所有抢购成功的用户，预热时用来重建 Redis 的「一人一单」标记 */
    List<Integer> selectRecordUserIds(Integer activityId);

    /** 管理端：启用 / 停用 */
    int updateStatus(@Param("id") Integer id, @Param("status") String status);

    /** 取消秒杀订单 / 删除活动时回补活动库存 */
    int restoreStock(@Param("id") Integer id, @Param("num") Integer num);

    /** 订单是不是秒杀单；返回 null 表示普通订单 */
    Integer selectActivityIdByOrderId(@Param("orderId") Integer orderId);

    /** 管理端：删除活动 */
    int deleteById(Integer id);
}
