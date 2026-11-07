package com.aiforge.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.aiforge"})
@MapperScan("com.aiforge.**.mapper")
public class AiforgeWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiforgeWebApplication.class, args);
        System.out.println("""
            
                ___    ________                            
               /   |  /  _/ __/___  _________ ____  
              / /| |  / // /_/ __ \\/ ___/ __ `/ _ \\ 
             / ___ |_/ // __/ /_/ / /  / /_/ /  __/ 
            /_/  |_/___/_/  \\____/_/   \\__, /\\___/  
                                      /____/        
            ===================================================
                             ! 项目启动成功 !
            ===================================================
            """);
    }

}
