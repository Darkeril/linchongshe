package com.hongshu.system;

import com.hongshu.common.security.annotation.EnableCustomConfig;
import com.hongshu.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 系统模块
 *
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class HongShuSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(HongShuSystemApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  系统模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
