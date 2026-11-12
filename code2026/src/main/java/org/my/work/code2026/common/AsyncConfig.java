package org.my.work.code2026.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 秒杀下单线程池。
 * <p>
 * 参数都是按「秒杀峰值」设的，面试被问线程池时可以直接拿它讲：
 * 核心 4 个线程常驻，队列 500 个缓冲，超过 500 后由 Tomcat 线程自己执行（CallerRunsPolicy），
 * 用背压代替直接丢任务——丢任务意味着用户钱没扣、订单没生成，比接口变慢严重得多。
 **/
@Configuration
@EnableAsync
public class AsyncConfig {

    public static final String SECKILL_EXECUTOR = "seckillExecutor";

    @Bean(SECKILL_EXECUTOR)
    public ThreadPoolTaskExecutor seckillExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(500);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("seckill-order-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
