package org.my.work.code2026.service;

import jakarta.annotation.Resource;
import org.my.work.code2026.entity.Goods;
import org.my.work.code2026.mapper.GoodsMapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiTools {

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsMapper goodsMapper;

    @Tool(description = "根据商品名称关键词搜索小卖部在售商品。用于回答'有没有辣条'、'可乐多少钱'这类问题")
    public String searchGoods(
            @ToolParam(description = "商品名称关键词，比如'辣条'、'可乐'") String keyword) {
        Goods q = new Goods();
        q.setName(keyword);
        q.setStatus("上架");
        return format(goodsService.selectAll(q), "没有找到与\"" + keyword + "\"相关的商品");
    }

    @Tool(description = "查询小卖部所有正在售卖的商品。当用户问'你们有什么商品'、'都卖什么'时使用")
    public String getAllGoods() {
        Goods q = new Goods();
        q.setStatus("上架");
        return format(goodsService.selectAll(q), "当前没有在售商品");
    }

    @Tool(description = "根据分类ID查询该分类下的在售商品。当用户问某个分类有什么时使用")
    public String getGoodsByCategory(
            @ToolParam(description = "商品分类ID，整数") Integer categoryId) {
        Goods q = new Goods();
        q.setCategoryId(categoryId);
        q.setStatus("上架");
        return format(goodsService.selectAll(q), "该分类下暂无在售商品");
    }

    @Tool(description = "根据商品ID查询商品详细信息。当用户询问某个具体商品详情时使用")
    public String getGoodsDetail(
            @ToolParam(description = "商品ID，整数") Integer id) {
        Goods g = goodsService.selectById(id);
        if (g == null) return "未找到ID为 " + id + " 的商品";
        return String.format(
                "[商品:%s|id:%d] 价格：%s 销量：%d",
                g.getName(),
                g.getId(),
                g.getPrice(),
                g.getSaleCount() == null ? 0 : g.getSaleCount());
    }

    @Tool(description = "查询小卖部推荐/精选商品。当用户问'有什么推荐的'时使用")
    public String getRecommendedGoods() {
        return format(goodsMapper.selectRecommended(), "暂无推荐商品");
    }

    @Tool(description = "查询小卖部销量最高的热门商品。当用户问'什么卖得好'时使用")
    public String getHotGoods() {
        return format(goodsMapper.selectHotGoods(), "暂无销售数据");
    }

    private String format(List<Goods> list, String emptyMsg) {
        if (list == null || list.isEmpty()) return emptyMsg;
        return list.stream().limit(20)
                .map(g -> String.format(
                        "[商品:%s|id:%d] 价格：%s 销量：%d",
                        g.getName(),
                        g.getId(),
                        g.getPrice(),
                        g.getSaleCount() == null ? 0 : g.getSaleCount()))
                .collect(Collectors.joining("\n"));
    }

    private String blank(String s) {
        return (s == null || s.isBlank()) ? "暂无简介" : s;
    }
}
