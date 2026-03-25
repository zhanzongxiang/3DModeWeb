package com.modelhub.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.modelhub.backend.mapper")
public class ModelHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModelHubApplication.class, args);
    }
}

