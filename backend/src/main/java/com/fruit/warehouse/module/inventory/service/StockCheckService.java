package com.fruit.warehouse.module.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.inventory.dto.StockCheckApproveRequest;
import com.fruit.warehouse.module.inventory.dto.StockCheckCreateRequest;
import com.fruit.warehouse.module.inventory.entity.StockCheckOrder;

public interface StockCheckService extends IService<StockCheckOrder> {
    StockCheckOrder createOrder(StockCheckCreateRequest request);

    StockCheckOrder approve(Long orderId, StockCheckApproveRequest request);

    IPage<StockCheckOrder> pageList(int pageNo, int pageSize, Long warehouseId, String status);
}
