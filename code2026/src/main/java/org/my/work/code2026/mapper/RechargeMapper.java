package org.my.work.code2026.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.my.work.code2026.entity.Recharge;

import java.util.List;

/**
 * 操作recharge相关数据接口
*/
@Mapper
public interface RechargeMapper {

    /**
      * 新增
    */
    int insert(Recharge recharge);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Recharge recharge);

    /**
      * 根据ID查询
    */
    Recharge selectById(Integer id);

    /**
      * 查询所有
    */
    List<Recharge> selectAll(Recharge recharge);

}