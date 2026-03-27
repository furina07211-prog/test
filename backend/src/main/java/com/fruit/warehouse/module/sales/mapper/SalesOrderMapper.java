package com.fruit.warehouse.module.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
}
