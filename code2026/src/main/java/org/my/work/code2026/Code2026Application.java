package org.my.work.code2026;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@MapperScan("org.my.work.code2026.mapper")
public class Code2026Application {

    public static void main(String[] args) {
        SpringApplication.run(Code2026Application.class, args);
    }
}