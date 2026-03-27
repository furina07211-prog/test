package com.fruit.warehouse.module.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderCreateRequest;
import com.fruit.warehouse.module.purchase.dto.PurchaseReceiveRequest;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrder;

public interface PurchaseService extends IService<PurchaseOrder> {
    PurchaseOrder createOrder(PurchaseOrderCreateRequest request);

    PurchaseOrder submit(Long orderId);

    PurchaseOrder approve(Long orderId);

    PurchaseOrder receive(Long orderId, PurchaseReceiveRequest request);

    IPage<PurchaseOrder> pageList(int pageNo, int pageSize, String status, Long supplierId);
}
