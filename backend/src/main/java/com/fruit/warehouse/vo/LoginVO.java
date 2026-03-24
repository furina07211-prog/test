package com.fruit.warehouse.vo;

import lombok.Data;

import java.util.List;

@Data
public class LoginVO {

    private String token;
    private UserVO userInfo;
    private List<String> permissions;
}
