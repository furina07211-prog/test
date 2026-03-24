package com.fruit.warehouse.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlertType {

    LOW_STOCK(1, "库存不足"),
    EXPIRING_SOON(2, "即将过期"),
    EXPIRED(3, "已过期");

    private final int code;
    private final String desc;
}
