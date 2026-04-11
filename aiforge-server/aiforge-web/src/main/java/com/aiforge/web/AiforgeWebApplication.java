package com.aiforge.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.aiforge"})
@MapperScan("com.aiforge.**.mapper")
public class AiforgeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiforgeWebApplication.class, args);
        System.out.println("项目启动成功");
    }

}
