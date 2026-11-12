package org.my.work.code2026.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;

import cn.hutool.core.util.RandomUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.GoodsCache;
import org.my.work.code2026.common.OrderDeadline;
import org.my.work.code2026.common.SeckillKeys;
import org.my.work.code2026.entity.*;
import org.my.work.code2026.exception.CustomException;
import org.my.work.code2026.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    private static final Logger log = LoggerFactory.getLogger(OrdersService.class);

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

    @Resource
    SeckillActivityMapper seckillActivityMapper;

    @Resource
    StringRedisTemplate redis;

    @Resource
    GoodsCache goodsCache;

    @Resource
    OrderDeadline orderDeadline;

    /**
     * 购物车单个和多选的下单接口
     */
    @Transactional
    public void add(Orders orders) {
        orders.setStatus("待接单");
        orders.setTime(DateUtil.now());
        // 只有新下单的订单带关单截止时间，历史订单该字段为 NULL，定时任务不会误伤
        orders.setCloseDeadline(orderDeadline.next());
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
            int num = cart.getNum() == null ? 0 : cart.getNum();
            if (num <= 0) {
                throw new CustomException("商品数量不合法");
            }
            // 原子扣减：一条带 store >= num 条件的 UPDATE 同时完成「判库存 + 扣库存 + 加销量」。
            // 事务提交前该行被行锁保护，并发下单串行执行，影响行数 0 就是库存不足。
            // 不再 selectById 读出来 set 回去，避免两个请求读到同一份旧库存导致超卖、以及全字段更新互相覆盖。
            if (goodsMapper.deductStoreAndIncreaseSale(goodsId, num) == 0) {
                throw new CustomException(goods.getName() + "商品库存不足");
            }
            //新增订单详情
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setNum(num);
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

            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(num)));
        }
        BigDecimal account = user.getAccount() == null ? BigDecimal.ZERO : user.getAccount();
        if(account.compareTo(totalPrice) < 0){//用户余额小于订单总价格
            throw new CustomException("对不起您的帐户余额不足，请充值！");
        }
        // 原子扣余额：一条带 account >= amount 条件的 UPDATE。
        // 若用「select 出来 → subtract → updateById」，并发下单时多个事务会读到同一份余额，
        // 各自减完写回，最后只扣了一单的钱（丢失更新）。上面的读只用于提前给出友好提示。
        if (userMapper.deductAccount(orders.getUserId(), totalPrice) == 0) {
            throw new CustomException("对不起您的帐户余额不足，请充值！");
        }
        orders.setTotal(totalPrice);
        ordersMapper.updateById(orders);//更新订单总额
        // 下单改了商品库存和销量，事务提交后失效商品缓存
        goodsCache.evictAllAfterCommit();
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
     * 修改。手工取消和定时任务共用同一条幂等链路：
     * 先用条件更新抢「关单权」，抢到的那个才退款、回补库存，连点两次/多实例都不会重复退钱。
     */
    @Transactional
    public void updateById(Orders orders) {
        Orders old = ordersMapper.selectById(orders.getId());
        boolean firstCancel = "已取消".equals(orders.getStatus())
                && old != null && !"已取消".equals(old.getStatus());
        if (firstCancel) {
            if (ordersMapper.closeIfPending(orders.getId()) == 0) {
                return;     // 已被定时任务或另一个请求关掉了
            }
            refundAndRestore(old);
            goodsCache.evictAllAfterCommit();
            // 状态已由条件更新写入，这里不再全量覆盖，顺带避免前端传来的 total 覆盖真实金额
            return;
        }
        ordersMapper.updateById(orders);
    }

    /**
     * 定时任务调用：超时自动关单并退款。
     * 由独立的 Task Bean 调进来，@Transactional 代理生效；条件更新保证多实例下只执行一次。
     */
    @Transactional
    public void closeExpired(Integer orderId) {
        if (ordersMapper.closeIfPending(orderId) == 0) {
            return;
        }
        Orders order = ordersMapper.selectById(orderId);
        refundAndRestore(order);
        goodsCache.evictAllAfterCommit();
        log.info("超时自动关单成功 orderId={}, userId={}, 退款={}",
                orderId, order.getUserId(), order.getTotal());
    }

    /** 取消/关单的公共动作：退余额 + 回补库存（秒杀单退活动池，普通单退商品池） */
    private void refundAndRestore(Orders order) {
        User user = userMapper.selectById(order.getUserId());
        if (user == null) {
            throw new CustomException("下单用户不存在");
        }
        // 原子加余额，避免同一用户多笔订单并发取消时互相覆盖
        BigDecimal refund = order.getTotal() == null ? BigDecimal.ZERO : order.getTotal();
        userMapper.restoreAccount(order.getUserId(), refund);

        // 秒杀单的库存在「活动池」，普通单的库存在 goods.store，取消时不能退错池
        Integer activityId = seckillActivityMapper.selectActivityIdByOrderId(order.getId());
        OrderDetail query = new OrderDetail();
        query.setOrderId(order.getId());
        List<OrderDetail> details = orderDetailMapper.selectAll(query);
        int totalNum = 0;
        for (OrderDetail detail : details) {
            int num = detail.getNum() == null ? 0 : detail.getNum();
            if (num <= 0) {
                continue;
            }
            // 下单改成原子扣减后，回补也必须原子，否则并发取消时会用旧值覆盖新库存
            goodsMapper.decreaseSaleCount(detail.getGoodsId(), num);
            if (activityId == null) {
                goodsMapper.restoreStore(detail.getGoodsId(), num);
            }
            totalNum += num;
        }
        if (activityId != null && totalNum > 0) {
            // 活动池 DB 与 Redis 都要补回去，否则下次抢购会出现「库里有货、Redis 显示售罄」
            seckillActivityMapper.restoreStock(activityId, totalNum);
            redis.opsForValue().increment(SeckillKeys.stock(activityId), totalNum);
        }
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
        // 条件更新抢单，影响行数 0 说明另一个骑手已经先接走了
        if (ordersMapper.acceptIfPending(orderId, deliveryId) == 0) {
            throw new CustomException("手慢了，该订单已被其他骑手接走");
        }
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