package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.my.work.code2026.entity.Goods;

import java.util.List;

/**
 * 操作goods相关数据接口
*/
@Mapper
public interface GoodsMapper {

    /**
      * 新增
    */
    int insert(Goods goods);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Goods goods);

    /**
      * 根据ID查询
    */
    Goods selectById(Integer id);

    /**
      * 查询所有
    */
    List<Goods> selectAll(Goods goods);

    List<Goods> selectRecommended();

    List<Goods> selectHotGoods();

    void incrementViews(@Param("id") Integer id);

    /**
     * 原子扣减商品库存：带 store >= num 条件，返回 0 表示库存不足。
     * 用于创建秒杀活动时把库存「划拨」给活动。
     */
    @Update("update goods set store = store - #{num} where id = #{id} and store >= #{num}")
    int deductStore(@Param("id") Integer id, @Param("num") Integer num);

    /** 原子增加销量，避免 select 后 set 造成覆盖 */
    @Update("update goods set sale_count = sale_count + #{num} where id = #{id}")
    int increaseSaleCount(@Param("id") Integer id, @Param("num") Integer num);

    /** 删除秒杀活动时，把没卖掉的配额归还给商品普通库存 */
    @Update("update goods set store = store + #{num} where id = #{id}")
    int restoreStore(@Param("id") Integer id, @Param("num") Integer num);

    /**
     * 普通下单：把「判断库存」和「扣库存 + 加销量」合并成一条带条件的原子 UPDATE。
     * 影响行数 0 表示库存不足（或商品不存在）；事务提交前该行被行锁保护，并发下单不会超卖。
     */
    @Update("update goods set store = ifnull(store,0) - #{num}, "
            + "sale_count = ifnull(sale_count,0) + #{num} "
            + "where id = #{id} and ifnull(store,0) >= #{num}")
    int deductStoreAndIncreaseSale(@Param("id") Integer id, @Param("num") Integer num);

    /** 取消/关单：原子回退销量，greatest 兜底防止减成负数 */
    @Update("update goods set sale_count = greatest(ifnull(sale_count,0) - #{num}, 0) where id = #{id}")
    int decreaseSaleCount(@Param("id") Integer id, @Param("num") Integer num);

}