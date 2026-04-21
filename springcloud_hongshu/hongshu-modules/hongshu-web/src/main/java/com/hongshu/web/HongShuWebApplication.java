package com.hongshu.web;

import com.hongshu.common.security.annotation.EnableCustomConfig;
import com.hongshu.common.security.annotation.EnableRyFeignClients;
import lombok.extern.log4j.Log4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * web模块
 *
 */
@Log4j
@EnableAsync
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"com.hongshu"})
public class HongShuWebApplication {
    public static void main(String[] args) {
        // 设置headless模式，解决服务器环境验证码生成问题
        // 本地开发环境通常有图形界面，不需要此设置也能正常工作
        // 服务器环境（Linux）通常没有图形界面，必须设置headless模式
        System.setProperty("java.awt.headless", "true");
        
        SpringApplication.run(HongShuWebApplication.class, args);
        log.info("(♥◠‿◠)ﾉﾞ  Web模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
