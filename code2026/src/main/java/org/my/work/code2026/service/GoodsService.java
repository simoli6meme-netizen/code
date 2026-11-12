package org.my.work.code2026.service;

import cn.hutool.core.date.DateUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.GoodsCache;
import org.my.work.code2026.entity.Goods;
import org.my.work.code2026.mapper.CartMapper;
import org.my.work.code2026.mapper.GoodsMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    @Resource
    GoodsCache goodsCache;

    /**
     * 新增。写库成功后删缓存，下次读会重新回源（Cache-Aside）
     */
    public void add(Goods goods) {
        goods.setViews(0);
        goods.setSaleCount(0);
        String now = DateUtil.now();  // 2025-11-18 16:20:00
        goods.setTime(now);
        goodsMapper.insert(goods);
        goodsCache.evictAllAfterCommit();
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        goodsMapper.deleteById(id);
        //删除下架对应的商品
        cartMapper.deleteByGoodsId(id);
        goodsCache.evictAllAfterCommit();
    }

    /**
     * 修改
     */
    public void updateById(Goods goods) {
        goodsMapper.updateById(goods);
        if ("下架".equals(goods.getStatus())) {
            cartMapper.deleteByGoodsId(goods.getId());
        }
        goodsCache.evictAllAfterCommit();
    }

    /**
     * 根据ID查询（缓存）：查不到会缓存一个 60 秒的空值，防穿透
     */
    public Goods selectById(Integer id) {
        return goodsCache.query("detail:" + id,
                new TypeReference<Goods>() {},
                () -> goodsMapper.selectById(id));
    }

    /**
     * 查询所有（缓存）：只按真正参与 SQL 条件的三个字段生成 key，
     * 避免 Goods 里其它无关字段造成缓存键爆炸。
     */
    public List<Goods> selectAll(Goods goods) {
        Goods q = goods == null ? new Goods() : goods;
        String raw = nullToEmpty(q.getName()) + "|" + q.getCategoryId() + "|" + nullToEmpty(q.getStatus());
        String key = "list:" + GoodsCache.digest(raw);
        List<Goods> list = goodsCache.query(key,
                new TypeReference<List<Goods>>() {},
                () -> goodsMapper.selectAll(q));
        // 命中空值标记时返回空数组，别把 null 丢给前端
        return list == null ? Collections.emptyList() : list;
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    /**
     * 分页查询：带总数和页码，缓存键维度太多，这里保持直连数据库。
     * 管理端写多读少，不是缓存收益最大的地方；前台高频的 selectAll / selectById 才缓存。
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Goods> list = goodsMapper.selectAll(goods);
        return PageInfo.of(list);
    }

    /**
     * 浏览量：写得很频繁，不主动删缓存，允许 2 分钟内的短暂不一致，靠 TTL 最终一致。
     * 否则每次点开详情页都会清空整块缓存，反而失去缓存意义。
     */
    public void incrementViews(Integer id) {
        goodsMapper.incrementViews(id);
    }

    /** 调试：看当前 Redis 里有哪些商品缓存 key */
    public List<String> cacheKeys() {
        return goodsCache.keys();
    }

}