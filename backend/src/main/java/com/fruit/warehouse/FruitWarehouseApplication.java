package com.fruit.warehouse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan({
    "com.fruit.warehouse.module.user.mapper",
    "com.fruit.warehouse.module.basic.mapper",
    "com.fruit.warehouse.module.ai.mapper",
    "com.fruit.warehouse.module.dashboard.mapper",
    "com.fruit.warehouse.module.inventory.mapper",
    "com.fruit.warehouse.module.purchase.mapper",
    "com.fruit.warehouse.module.sales.mapper"
})
@SpringBootApplication
public class FruitWarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitWarehouseApplication.class, args);
    }
}
