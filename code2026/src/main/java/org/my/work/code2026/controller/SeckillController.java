package org.my.work.code2026.controller;

import jakarta.annotation.Resource;
import org.my.work.code2026.common.Result;
import org.my.work.code2026.entity.SeckillActivity;
import org.my.work.code2026.service.SeckillService;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀接口
 * <p>
 * 说明：项目目前还没有做登录鉴权，userId 由前端从 localStorage 里带过来，
 * 等后续接上 JWT，把 userId 改成从 token 里取即可，接口层不用动。
 **/
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    /** 活动列表：带实时剩余库存和「我是否已抢到」 */
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Integer userId) {
        return Result.success(seckillService.list(userId));
    }

    /** 抢购：返回 PENDING 表示已进入异步下单 */
    @PostMapping("/{id}")
    public Result seckill(@PathVariable Integer id, @RequestParam Integer userId) {
        return Result.success(seckillService.seckill(id, userId));
    }

    /** 轮询下单结果：PENDING / SUCCESS / FAIL / NOT_FOUND */
    @GetMapping("/result")
    public Result result(@RequestParam Integer activityId, @RequestParam Integer userId) {
        return Result.success(seckillService.result(activityId, userId));
    }

    /** 手动预热（活动开始前调一次，或 Redis 重启后补数据） */
    @PostMapping("/preheat/{id}")
    public Result preheat(@PathVariable Integer id) {
        seckillService.preheat(id);
        return Result.success();
    }

    /** 管理端：创建活动，库存会从商品普通库存里划拨过来 */
    @PostMapping("/activity")
    public Result create(@RequestBody SeckillActivity activity) {
        return Result.success(seckillService.create(activity));
    }

    /** 管理端：启用 / 停用，例如 PUT /seckill/activity/1/status?status=停用 */
    @PutMapping("/activity/{id}/status")
    public Result toggleStatus(@PathVariable Integer id, @RequestParam String status) {
        seckillService.toggleStatus(id, status);
        return Result.success();
    }

    /** 管理端：删除活动（有人抢购过则拒绝，只能停用） */
    @DeleteMapping("/activity/{id}")
    public Result remove(@PathVariable Integer id) {
        seckillService.remove(id);
        return Result.success();
    }
}
