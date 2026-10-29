package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.my.work.code2026.entity.Cart;

import java.util.List;

/**
 * 操作cart相关数据接口
*/
@Mapper
public interface CartMapper {

    /**
      * 新增
    */
    int insert(Cart cart);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Cart cart);

    /**
      * 根据ID查询
    */
    Cart selectById(Integer id);

    /**
      * 查询所有
    */
    List<Cart> selectAll(Cart cart);

    @Select("select * from cart where goods_id = #{goodsId} and user_id = #{userId}")
    Cart selectByGoodsIdAndUserId(@Param("goodsId") Integer goodsId,@Param("userId") Integer userId);

    @Delete("delete from cart where goods_id = #{goodsId}")
    void deleteByGoodsId(Integer goodsId);
}