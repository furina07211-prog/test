package com.fruit.warehouse.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BatchStatus {

    NORMAL(1, "正常"),
    EXPIRING(2, "即将过期"),
    EXPIRED(3, "已过期"),
    DEPLETED(4, "已耗尽");

    private final int code;
    private final String desc;
}
