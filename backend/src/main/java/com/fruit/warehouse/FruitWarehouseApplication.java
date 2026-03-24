package com.fruit.warehouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.fruit.warehouse.mapper")
@EnableScheduling
public class FruitWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitWarehouseApplication.class, args);
    }
}
