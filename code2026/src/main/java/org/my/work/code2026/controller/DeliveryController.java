package org.my.work.code2026.controller;

import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.Result;
import org.my.work.code2026.entity.Delivery;
import org.my.work.code2026.service.DeliveryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    @Resource
    private DeliveryService deliveryService;

    /**
     * 分页查询外卖员
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String name) {
        PageInfo<Delivery> pageInfo = deliveryService.selectPage(pageNum, pageSize, name);
        return Result.success(pageInfo);
    }

    /**
     * 根据 ID 查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Delivery delivery = deliveryService.selectById(id);
        return Result.success(delivery);
    }

    /**
     * 删除外卖员
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        deliveryService.deleteById(id);
        return Result.success();
    }

    /**
     * 新增外卖员
     */
    @PostMapping("/add")
    public Result add(@RequestBody Delivery delivery) {
        deliveryService.add(delivery);
        return Result.success();
    }

    /**
     * 修改外卖员
     */
    @PutMapping("/update")
    public Result update(@RequestBody Delivery delivery) {
        deliveryService.update(delivery);
        return Result.success();
    }
}
