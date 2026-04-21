package com.hongshu.job;

import com.hongshu.common.security.annotation.EnableCustomConfig;
import com.hongshu.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 定时任务
 *

 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HongShuJobApplication {
    public static void main(String[] args) {
        SpringApplication.run(HongShuJobApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  定时任务模块启动成功   ლ(´ڡ`ლ)ﾞ ");
    }
}
