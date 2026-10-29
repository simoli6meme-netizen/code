package org.my.work.code2026.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.RandomUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.entity.*;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员业务处理
 **/
@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    GoodsMapper goodsMapper;

    @Resource
    UserMapper userMapper;

    @Resource
    OrderDetailMapper orderDetailMapper;

    @Resource
    CartMapper cartMapper;

    /**
     * 购物车单个和多选的下单接口
     */
    @Transactional
    public void add(Orders orders) {
        orders.setStatus("待接单");
        orders.setTime(DateUtil.now());
        //随机订单编号
        String orderNo = DateUtil.format(new Date(),"yyyyMMDD"  + RandomUtil.randomNumbers(4));
        orders.setOrderNo(orderNo);
        ordersMapper.insert(orders);
        Integer orderId = orders.getId();
        List<Cart> cartList = orders.getCartList();
        if (cartList == null || cartList.isEmpty()) {
            throw new CustomException("请选择要下单的商品");
        }
        User user = userMapper.selectById(orders.getUserId());
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("下单用户不存在");
        }
        BigDecimal totalPrice = BigDecimal.ZERO;
        for(Cart cart : cartList){
            Integer goodsId = cart.getGoodsId();
            Goods goods = goodsMapper.selectById(goodsId);
            if (ObjectUtil.isNull(goods)) {
                throw new CustomException("商品不存在或已下架，请刷新后重试");
            }
            if (goods.getStore() == null) {
                goods.setStore(0);
            }
            if(goods.getStore() < cart.getNum()){//库存不足
                throw new CustomException(goods.getName() + "商品库存不足");
            }
            goods.setStore(goods.getStore() - cart.getNum());//扣减库存
            goods.setSaleCount(goods.getSaleCount() + cart.getNum());//增加销量
            goodsMapper.updateById(goods);//更新商品的库存
            //新增订单详情
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setNum(cart.getNum());
            orderDetail.setGoodsId(goodsId);
            orderDetail.setGoodsImg(goods.getImg());
            orderDetail.setGoodsName(goods.getName());
            orderDetail.setOrderId(orderId);
            orderDetail.setGoodsPrice(goods.getPrice());
            orderDetailMapper.insert(orderDetail);

            //删除下单商品所对应的购物车记录
            if(cart.getId() != null){
                cartMapper.deleteById(cart.getId());
            }

            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));
        }
        BigDecimal account = user.getAccount() == null ? BigDecimal.ZERO : user.getAccount();
        if(account.compareTo(totalPrice) < 0){//用户余额小于订单总价格
            throw new CustomException("对不起您的帐户余额不足，请充值！");
        }
        user.setAccount(user.getAccount().subtract(totalPrice));
        userMapper.updateById(user);//更新余额
        orders.setTotal(totalPrice);
        ordersMapper.updateById(orders);//更新订单总额
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
        ordersMapper.deleteByOrderId(id);
    }

    /**
     * 修改
     */
    @Transactional
    public void updateById(Orders orders) {
        if ("已取消".equals(orders.getStatus())) {
            Integer userId = orders.getUserId();
            User user = userMapper.selectById(userId);
            user.setAccount(user.getAccount().add(orders.getTotal()));
            userMapper.updateById(user);
            //商品库存修改,销量修改
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            for(OrderDetail detail : orderDetailList){
                Integer goodsId = detail.getGoodsId();
                Goods goods = goodsMapper.selectById(goodsId);
                if(goods != null){
                    goods.setStore(goods.getStore() + detail.getNum());
                    goods.setSaleCount(goods.getSaleCount() - detail.getNum());
                    goodsMapper.updateById(goods);
                }
            }
        }
        ordersMapper.updateById(orders);
    }

    /**
     * 根据ID查询
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    /**
     * 分页查询
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        fillOrderDetail(list);
        return PageInfo.of(list);
    }

    /* ==================== 外卖员端相关 ==================== */

    /** 待接单 */
    public static final String STATUS_PENDING = "待接单";
    /** 已接单，骑手前往小卖部取货 */
    public static final String STATUS_DELIVERING = "配送中";
    /** 已取货，正在送往用户（用户端显示「待收货」） */
    public static final String STATUS_WAIT_RECEIVE = "待收货";
    /** 骑手已送达，等待用户确认 */
    public static final String STATUS_DELIVERED = "已送达";
    /** 用户已确认收货 */
    public static final String STATUS_DONE = "已完成";

    /**
     * 查询订单（带商品明细），供外卖员端列表使用
     */
    public List<Orders> selectAllWithDetail(Orders orders) {
        List<Orders> list = ordersMapper.selectAll(orders);
        fillOrderDetail(list);
        return list;
    }

    /**
     * 按条件统计订单数量
     */
    public int count(Orders orders) {
        return ordersMapper.selectCount(orders);
    }

    /**
     * 外卖员接单：绑定外卖员并把状态置为「配送中」
     */
    @Transactional
    public void accept(Integer orderId, Integer deliveryId) {
        Orders order = ordersMapper.selectById(orderId);
        if (ObjectUtil.isNull(order)) {
            throw new CustomException("订单不存在");
        }
        if (!STATUS_PENDING.equals(order.getStatus())) {
            throw new CustomException("手慢了，该订单已被其他骑手接走");
        }
        Orders update = new Orders();
        update.setId(orderId);
        update.setDeliveryId(deliveryId);
        update.setStatus(STATUS_DELIVERING);
        ordersMapper.updateById(update);
    }

    /**
     * 外卖员工作台统计
     * pending：全部「待接单」的外送订单
     * delivering：我名下配送中 / 待收货 / 已送达 的订单
     * completed：我名下已完成的订单
     */
    public Map<String, Integer> deliveryStats(Integer deliveryId) {
        Orders pendingQuery = new Orders();
        pendingQuery.setStatus(STATUS_PENDING);
        pendingQuery.setDeliverType("外送");
        pendingQuery.setUnassigned(true);
        int pending = ordersMapper.selectCount(pendingQuery);

        Orders deliveringQuery = new Orders();
        deliveringQuery.setDeliveryId(deliveryId);
        deliveringQuery.setStatuses(List.of(STATUS_DELIVERING, STATUS_WAIT_RECEIVE, STATUS_DELIVERED));
        int delivering = ordersMapper.selectCount(deliveringQuery);

        Orders completedQuery = new Orders();
        completedQuery.setDeliveryId(deliveryId);
        completedQuery.setStatus(STATUS_DONE);
        int completed = ordersMapper.selectCount(completedQuery);

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("pending", pending);
        stats.put("delivering", delivering);
        stats.put("completed", completed);
        return stats;
    }

    /**
     * 批量填充订单的商品明细
     */
    private void fillOrderDetail(List<Orders> list) {
        if (list == null) return;
        for (Orders o : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(o.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            o.setOrderDetailList(orderDetailList);
        }
    }

}