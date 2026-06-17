package com.pm.scheduler;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pm.scheduler.mapper")
public class PMSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PMSchedulerApplication.class, args);
    }
}
