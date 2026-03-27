package com.fruit.warehouse.module.inventory.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.inventory.dto.StockCheckApproveRequest;
import com.fruit.warehouse.module.inventory.dto.StockCheckCreateRequest;
import com.fruit.warehouse.module.inventory.entity.StockCheckOrder;

/**
 * 库存管理 模块服务接口。
 */
public interface StockCheckService extends IService<StockCheckOrder> {
    /**
     * 创建盘点单。
     */
    StockCheckOrder createOrder(StockCheckCreateRequest request);

    /**
     * 审核盘点单并执行库存调整。
     */
    StockCheckOrder approve(Long orderId, StockCheckApproveRequest request);

    /**
     * 分页查询盘点单。
     */
    IPage<StockCheckOrder> pageList(int pageNo, int pageSize, Long warehouseId, String status);
}
