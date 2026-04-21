package com.hongshu.ai;

import com.hongshu.common.security.annotation.EnableCustomConfig;
import com.hongshu.common.security.annotation.EnableRyFeignClients;
import lombok.extern.log4j.Log4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * idle服务
 *

 */
@Log4j
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"com.hongshu.common.core.utils", "com.hongshu.ai"})
public class HongShuAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HongShuAiApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  ai服务模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
