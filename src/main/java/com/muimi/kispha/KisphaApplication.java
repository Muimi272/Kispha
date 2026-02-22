package com.muimi.kispha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Kispha 应用程序启动类
 *
 * 此类是整个 Kispha 项目的入口点，通过 Spring Boot 自动配置来初始化应用上下文
 * 并启动应用服务器。
 *
 * @author Muimi272
 * @version 1.0
 * @since 2026-02-22
 */
@SpringBootApplication
public class KisphaApplication {

    /**
     * 应用程序主方法
     *
     * 用于启动 Spring Boot 应用，初始化容器并加载所有配置的 Bean。
     *
     * @param args 命令行参数（通常为空）
     */
    public static void main(String[] args) {
        // 启动 Spring Boot 应用
        SpringApplication.run(KisphaApplication.class, args);
    }

}
