package org.my.work.code2026.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

/**
 * 把 lua/seckill.lua 加载成一个 Spring Bean。
 * <p>
 * 注意：StringRedisTemplate 由 Spring Boot 自动装配，不需要手动配置，
 * 而且它用 String 序列化，Lua 里才能直接 tonumber(GET 的结果)。
 **/
@Configuration
public class SeckillRedisConfig {

    @Bean
    public DefaultRedisScript<Long> seckillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/seckill.lua"));
        script.setResultType(Long.class);
        return script;
    }
}
