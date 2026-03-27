package com.fruit.warehouse.module.basic.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fruit_info")
public class Fruit {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fruitCode;
    private String fruitName;
    private Long categoryId;
    private String unit;
    private String originPlace;
    private Integer shelfLifeDays;
    private Integer warningDays;
    private java.math.BigDecimal safeStockQty;
    private java.math.BigDecimal suggestedPurchasePrice;
    private java.math.BigDecimal suggestedSalePrice;
    private Integer status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
