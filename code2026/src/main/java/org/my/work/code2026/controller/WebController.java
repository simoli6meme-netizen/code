package org.my.work.code2026.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import jakarta.annotation.Resource;
import org.my.work.code2026.cache.CaptchaCache;
import org.my.work.code2026.common.Result;
import org.my.work.code2026.entity.*;
import org.my.work.code2026.mapper.OrderDetailMapper;
import org.my.work.code2026.service.*;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;


@RestController
public class WebController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @Resource
    OrdersService ordersService;

    @Resource
    GoodsService goodsService;

    @Resource
    CategoryService categoryService;

    @Resource
    OrderDetailMapper orderDetailMapper;

    @Resource
    private CaptchaCache captchaCache;

    @Resource
    private DeliveryService deliveryService;
    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }


    /**
     * 生成验证码图片
     * 前端：<img src="/captcha?key=UUID">
     */
    @GetMapping(value = "/captcha", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> captcha(@RequestParam String key) {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 6);
        captchaCache.put(key, captcha.getCode(), 300);   // 5 分钟有效

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_PNG)
                .body(captcha.getImageBytes());
    }
    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        String role = account.getRole();

        if ("管理员".equals(role)) {
            ac = adminService.login(account);
        } else if ("普通用户".equals(role)) {
            ac = userService.login(account);
        } else if ("外卖员".equals(role)) {
            ac = deliveryService.login(account);   // ← 新增这一支
        } else {
            return Result.error("请选择正确的身份");
        }

        if (ac == null) {
            return Result.error("登录失败，账号或密码错误");
        }
        return Result.success(ac);
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {

        // ===== 1. 验证码校验（公共逻辑）=====
        String realCode = captchaCache.getAndRemove(user.getCaptchaKey());
        if (realCode == null) {
            return Result.error("验证码已过期，请刷新");
        }
        if (user.getCaptcha() == null
                || !realCode.equalsIgnoreCase(user.getCaptcha().trim())) {
            return Result.error("验证码错误");
        }

        // ===== 2. 密码一致性校验（公共逻辑）=====
        if (!user.getPassword().equals(user.getNewPassword())) {
            return Result.error("两次输入密码不一致");
        }

        // ===== 3. 按角色分流 =====
        if ("外卖员".equals(user.getRole())) {
            // 外卖员：拷贝到 Delivery 表
            Delivery delivery = new Delivery();
            delivery.setUsername(user.getUsername());
            delivery.setPassword(user.getPassword());
            deliveryService.add(delivery);
            return Result.success("注册成功");
        }

        // ===== 4. 普通用户：原逻辑不变 =====
        userService.add(user);
        return Result.success();
    }
    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("管理员".equals(account.getRole())) {
            adminService.updatePassword(account);
        }
        if ("普通用户".equals(account.getRole())) {
            userService.updatePassword(account);
        }
        if ("外卖员".equals(account.getRole())) {
            deliveryService.updatePassword(account);
        }
        return Result.success();
    }

    @GetMapping("/count")
    public Result count() {
        List<Orders> ordersList = ordersService.selectAll(null).stream().filter(orders -> !orders.getStatus().equals("已取消")).toList();
        BigDecimal total = ordersList.stream().map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        String todayDate = DateUtil.today();  // 2025-12-05
        BigDecimal today = ordersList.stream().filter(orders -> orders.getTime().contains(todayDate))
                .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        Integer goods = goodsService.selectAll(null).size();
        Integer user = userService.selectByAll(null).size();
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);
        map.put("today", today);
        map.put("goods", goods);
        map.put("user", user);
        return Result.success(map);
    }

    @GetMapping("/selectLine")
    public Result selectLine() {
        Date date = new Date();
        DateTime start = DateUtil.offsetDay(date, -6);
        List<DateTime> dateTimes = DateUtil.rangeToList(start, date, DateField.DAY_OF_YEAR);
        List<String> dateStrList = dateTimes.stream().map(dateTime -> DateUtil.format(dateTime, "MM-dd")).sorted().toList();
        List<Orders> ordersList = ordersService.selectAll(null).stream().filter(orders -> !orders.getStatus().equals("已取消")).toList();
        int year = DateUtil.year(date);
        ArrayList<BigDecimal> countList = new ArrayList<>();
        for (String day : dateStrList) {
            // 包含  年月日的  订单
            BigDecimal total = ordersList.stream().filter(o -> o.getTime().contains(String.valueOf(year)) && o.getTime().contains(day))
                    .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            countList.add(total);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("date", dateStrList);
        map.put("count", countList);
        return Result.success(map);
    }


    @GetMapping("/selectPie")
    public Result selectPie() {
        List<Map<String, Object>> list = new ArrayList<>();
        List<Category> categoryList = categoryService.selectAll(null);
        Map<String, Object> map;
        for (Category category : categoryList) {
            map = new HashMap<>();
            map.put("name", category.getName());
            BigDecimal total = BigDecimal.ZERO;
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(null);
            for (OrderDetail orderDetail : orderDetailList) {
                Integer orderId = orderDetail.getOrderId();
                Orders orders = ordersService.selectById(orderId);
                if (!orders.getStatus().equals("已取消")) {
                    Integer goodsId = orderDetail.getGoodsId();
                    Goods goods = goodsService.selectById(goodsId);
                    if (goods.getCategoryId().equals(category.getId())) {
                        total = total.add(orders.getTotal());
                    }
                }
            }
            map.put("value", total);
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                list.add(map);
            }
        }
        return Result.success(list);
    }
}









