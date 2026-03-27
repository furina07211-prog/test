package com.fruit.warehouse.module.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.warehouse.module.inventory.entity.StockCheckOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockCheckOrderMapper extends BaseMapper<StockCheckOrder> {
}
