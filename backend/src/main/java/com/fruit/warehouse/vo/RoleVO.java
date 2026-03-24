package com.fruit.warehouse.vo;

import lombok.Data;

@Data
public class RoleVO {

    private Long id;
    private String roleName;
    private String roleCode;
    private String description;
    private Integer status;
}
