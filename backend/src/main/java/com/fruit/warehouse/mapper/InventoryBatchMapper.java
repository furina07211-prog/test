package com.fruit.warehouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruit.warehouse.entity.InventoryBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InventoryBatchMapper extends BaseMapper<InventoryBatch> {

    @Select("SELECT * FROM inventory_batch " +
            "WHERE product_id = #{productId} AND warehouse_id = #{warehouseId} " +
            "AND remaining_quantity > 0 AND batch_status IN (1, 2) AND deleted = 0 " +
            "ORDER BY expiry_date ASC")
    List<InventoryBatch> selectAvailableBatchesFEFO(@Param("productId") Long productId, 
                                                     @Param("warehouseId") Long warehouseId);
}
