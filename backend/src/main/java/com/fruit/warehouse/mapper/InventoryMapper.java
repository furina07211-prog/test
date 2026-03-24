package com.fruit.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.warehouse.entity.Inventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    @Select("SELECT * FROM inventory WHERE product_id = #{productId} AND warehouse_id = #{warehouseId}")
    Inventory selectByProductAndWarehouse(@Param("productId") Long productId, @Param("warehouseId") Long warehouseId);
}
