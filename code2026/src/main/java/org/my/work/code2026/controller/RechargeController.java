package org.my.work.code2026.controller;


import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.my.work.code2026.common.Result;
import org.my.work.code2026.entity.Recharge;
import org.my.work.code2026.service.RechargeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员前端操作接口
 **/
@RestController
@RequestMapping("/recharge")
public class RechargeController {

    @Resource
    private RechargeService rechargeService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Recharge recharge) {
        rechargeService.add(recharge);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        rechargeService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Recharge recharge) {
        rechargeService.updateById(recharge);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Recharge recharge = rechargeService.selectById(id);
        return Result.success(recharge);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Recharge recharge) {
        List<Recharge> list = rechargeService.selectAll(recharge);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Recharge recharge,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Recharge> page = rechargeService.selectPage(recharge, pageNum, pageSize);
        return Result.success(page);
    }

}