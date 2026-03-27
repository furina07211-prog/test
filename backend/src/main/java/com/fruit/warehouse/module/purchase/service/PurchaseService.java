package com.fruit.warehouse.module.purchase.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderCreateRequest;
import com.fruit.warehouse.module.purchase.dto.PurchaseOrderPageQuery;
import com.fruit.warehouse.module.purchase.dto.PurchaseReceiveRequest;
import com.fruit.warehouse.module.purchase.entity.PurchaseOrder;
import com.fruit.warehouse.module.purchase.vo.PurchaseOrderItemVO;
import com.fruit.warehouse.module.purchase.vo.PurchaseOrderPageVO;
import java.util.List;

/**
 * 采购管理 模块服务接口。
 */
public interface PurchaseService extends IService<PurchaseOrder> {
    /**
     * 创建采购单草稿。
     */
    PurchaseOrder createOrder(PurchaseOrderCreateRequest request);

    /**
     * 提交采购单。
     */
    PurchaseOrder submit(Long orderId);

    /**
     * 审核采购单。
     */
    PurchaseOrder approve(Long orderId);

    /**
     * 按明细分批收货并更新库存。
     */
    PurchaseOrder receive(Long orderId, PurchaseReceiveRequest request);

    /**
     * 分页查询采购单。
     */
    IPage<PurchaseOrderPageVO> pageList(PurchaseOrderPageQuery query);

    /**
     * 查询采购单明细。
     */
    List<PurchaseOrderItemVO> listItems(Long orderId);
}
