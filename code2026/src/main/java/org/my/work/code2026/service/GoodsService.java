package org.my.work.code2026.service;

import cn.hutool.core.date.DateUtil;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Goods;
import org.my.work.code2026.mapper.CartMapper;
import org.my.work.code2026.mapper.GoodsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员业务处理
 **/
@Service
public class GoodsService {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    CartMapper cartMapper;

    /**
     * 新增
     */
    public void add(Goods goods) {
        goods.setViews(0);
        goods.setSaleCount(0);
        String now = DateUtil.now();  // 2025-11-18 16:20:00
        goods.setTime(now);
        goodsMapper.insert(goods);
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
        //删除下架对应的商品
        cartMapper.deleteByGoodsId(id);
    }

    /**
     * 修改
     */
    public void updateById(Goods goods) {
        goodsMapper.updateById(goods);
        if ("下架".equals(goods.getStatus())) {
            cartMapper.deleteByGoodsId(goods.getId());
        }
    }

    /**
     * 根据ID查询
     */
    public Goods selectById(Integer id) {
        return goodsMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Goods> selectAll(Goods goods) {
        return goodsMapper.selectAll(goods);
    }

    /**
     * 分页查询
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }

    public void incrementViews(Integer id) {
        goodsMapper.incrementViews(id);
    }

}