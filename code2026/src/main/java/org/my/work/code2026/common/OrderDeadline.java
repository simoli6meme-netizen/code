package org.my.work.code2026.common;

import cn.hutool.core.date.DateUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 统一的「自动关单截止时间」计算器。
 * 下单时给订单写一个未来时间戳，定时任务只扫描这个字段非空的订单——
 * 这样历史订单（字段为 NULL）永远不会被误取消。
 **/
@Component
public class OrderDeadline {

    @Value("${order.close-minutes:15}")
    private int closeMinutes;

    public String next() {
        return DateUtil.format(DateUtil.offsetMinute(new Date(), closeMinutes), "yyyy-MM-dd HH:mm:ss");
    }

    public int getCloseMinutes() {
        return closeMinutes;
    }
}
