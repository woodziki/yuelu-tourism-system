package com.yuelu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类。
 *
 * <p>本项目按 PRD 要求使用 Spring Boot + MyBatis-Plus + MySQL。
 * 这里保持最小化启动入口，便于新手理解与后续逐步扩展。</p>
 */
@SpringBootApplication
@MapperScan("com.yuelu.mapper")
public class YueluTourismApplication {

    public static void main(String[] args) {
        SpringApplication.run(YueluTourismApplication.class, args);
    }
}

