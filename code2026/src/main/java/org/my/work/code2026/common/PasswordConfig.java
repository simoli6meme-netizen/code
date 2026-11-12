package org.my.work.code2026.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密配置。
 *
 * 只引入 spring-security-crypto 里的 BCrypt 能力，不引入 spring-boot-starter-security，
 * 因此不会额外注册任何登录过滤器，对现有接口零影响。
 *
 * BCrypt 自带随机盐，同一个密码每次加密结果都不同，所以只能 matches 不能比字符串。
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
