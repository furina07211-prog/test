package com.fruit.warehouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fruit.warehouse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private String productCode;
    private String productName;
    private Long categoryId;
    private String unit;
    private String spec;
    private Integer shelfLifeDays;
    private Integer lowStockThreshold;
    private String imageUrl;
    private String description;
    private Integer status;
}
