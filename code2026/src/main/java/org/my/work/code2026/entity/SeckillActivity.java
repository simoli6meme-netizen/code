package org.my.work.code2026.entity;

import java.math.BigDecimal;

/**
 * 秒杀活动
 * <p>
 * 前 8 个字段对应 seckill_activity 表，后面几个是联表查询与运行时填充字段，
 * 不落库（selectAll 里通过 join goods 查出来，remain / bought 由 Redis 填充）。
 **/
public class SeckillActivity {

    private Integer id;
    /** 商品ID */
    private Integer goodsId;
    /** 秒杀价 */
    private BigDecimal seckillPrice;
    /** 活动库存（数据库中的权威值） */
    private Integer stock;
    /** 开始时间 */
    private String startTime;
    /** 结束时间 */
    private String endTime;
    /** 启用 / 停用 */
    private String status;
    /** 创建时间 */
    private String createTime;

    /** 商品名称（联表） */
    private String goodsName;
    /** 商品图片（联表） */
    private String goodsImg;
    /** 商品原价（联表） */
    private BigDecimal originalPrice;
    /** 商品简介（联表） */
    private String goodsDescription;
    /** 活动状态：未开始 / 进行中 / 已结束，由数据库按时间算出来 */
    private String state;
    /** Redis 中的剩余库存，列表接口填充 */
    private Integer remain;
    /** 当前用户是否已抢到，列表接口填充 */
    private Boolean bought;
    /** 已抢件数（排除已取消订单），由 selectAll 子查询统计，不落库 */
    private Integer sold;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    public BigDecimal getSeckillPrice() {
        return seckillPrice;
    }

    public void setSeckillPrice(BigDecimal seckillPrice) {
        this.seckillPrice = seckillPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsImg() {
        return goodsImg;
    }

    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getGoodsDescription() {
        return goodsDescription;
    }

    public void setGoodsDescription(String goodsDescription) {
        this.goodsDescription = goodsDescription;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getRemain() {
        return remain;
    }

    public void setRemain(Integer remain) {
        this.remain = remain;
    }

    public Boolean getBought() {
        return bought;
    }

    public void setBought(Boolean bought) {
        this.bought = bought;
    }

    public Integer getSold() {
        return sold;
    }

    public void setSold(Integer sold) {
        this.sold = sold;
    }
}
