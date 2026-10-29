package org.my.work.code2026.controller;


import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.Result;
import org.my.work.code2026.entity.Orders;
import org.my.work.code2026.service.OrdersService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Resource
    private OrdersService ordersService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Orders orders) {
        ordersService.add(orders);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        ordersService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Orders orders = ordersService.selectById(id);
        return Result.success(orders);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Orders orders) {
        List<Orders> list = ordersService.selectAll(orders);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Orders orders,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 查询订单（带商品明细），外卖员端列表使用
     */
    @GetMapping("/selectAllWithDetail")
    public Result selectAllWithDetail(Orders orders) {
        List<Orders> list = ordersService.selectAllWithDetail(orders);
        return Result.success(list);
    }

    /**
     * 按条件统计订单数量
     */
    @GetMapping("/count")
    public Result count(Orders orders) {
        return Result.success(ordersService.count(orders));
    }

    /**
     * 外卖员接单：{ id: 订单ID, deliveryId: 外卖员ID }
     */
    @PostMapping("/accept")
    public Result accept(@RequestBody Orders orders) {
        ordersService.accept(orders.getId(), orders.getDeliveryId());
        return Result.success();
    }

    /**
     * 外卖员工作台统计：待接单 / 配送中 / 已完成
     */
    @GetMapping("/deliveryStats")
    public Result deliveryStats(@RequestParam Integer deliveryId) {
        return Result.success(ordersService.deliveryStats(deliveryId));
    }

}