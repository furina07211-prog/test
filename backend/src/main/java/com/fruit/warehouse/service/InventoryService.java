package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.vo.BatchVO;
import com.fruit.warehouse.vo.InventoryVO;

public interface InventoryService {

    Page<InventoryVO> page(Integer pageNum, Integer pageSize, String productName, Long warehouseId, Long categoryId);

    Page<BatchVO> pageBatches(Integer pageNum, Integer pageSize, Long productId, Long warehouseId, Integer status);

    InventoryVO getByProductAndWarehouse(Long productId, Long warehouseId);
}
