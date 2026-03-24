package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruit.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    private String permissionName;
    private String permissionCode;
    private String module;
    private String description;
}
