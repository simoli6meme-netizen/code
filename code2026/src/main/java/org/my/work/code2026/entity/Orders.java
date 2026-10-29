package org.my.work.code2026.entity;

import java.math.BigDecimal;
import java.util.List;

public class Orders {
    /**主键ID */
    private Integer id;
    /**订单编号 */
    private String orderNo;
    /**总价格 */
    private BigDecimal total;
    /**下单人ID */
    private Integer userId;
    /**外卖员ID（接单后写入） */
    private Integer deliveryId;
    /**外卖员姓名（联表查询返回） */
    private String deliveryName;
    /**状态 */
    private String status;
    /**下单时间 */
    private String time;
    /**配送类型 */
    private String deliverType;

    /**收货地址（地图坐标X） */
    private Double addressX;
    /**收货地址（地图坐标Y） */
    private Double addressY;
    /**是否只查询「未分配外卖员」的订单 */
    private Boolean unassigned;
    /**按多个状态查询（用于外卖端「配送中」列表） */
    private List<String> statuses;

    private String userName;

    private List<Cart> cartList;

    private List<OrderDetail> orderDetailList;

    private String address;

    private String deliver;

    private String goodsName;

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeliver() {
        return deliver;
    }

    public void setDeliver(String deliver) {
        this.deliver = deliver;
    }

    public List<Cart> getCartList() {
        return cartList;
    }

    public List<OrderDetail> getOrderDetailList() {
        return orderDetailList;
    }

    public void setOrderDetailList(List<OrderDetail> orderDetailList) {
        this.orderDetailList = orderDetailList;
    }

    public void setCartList(List<Cart> cartList) {
        this.cartList = cartList;
    }

    public Orders() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDeliverType() {
        return deliverType;
    }

    public void setDeliverType(String deliverType) {
        this.deliverType = deliverType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Integer deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getDeliveryName() {
        return deliveryName;
    }

    public void setDeliveryName(String deliveryName) {
        this.deliveryName = deliveryName;
    }

    public Double getAddressX() {
        return addressX;
    }

    public void setAddressX(Double addressX) {
        this.addressX = addressX;
    }

    public Double getAddressY() {
        return addressY;
    }

    public void setAddressY(Double addressY) {
        this.addressY = addressY;
    }

    public Boolean getUnassigned() {
        return unassigned;
    }

    public void setUnassigned(Boolean unassigned) {
        this.unassigned = unassigned;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }
}
