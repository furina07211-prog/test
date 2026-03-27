package com.fruit.warehouse.module.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fruit.warehouse.module.sales.dto.SalesOrderCreateRequest;
import com.fruit.warehouse.module.sales.dto.SalesOrderPageQuery;
import com.fruit.warehouse.module.sales.dto.SalesShipRequest;
import com.fruit.warehouse.module.sales.entity.SalesOrder;
import com.fruit.warehouse.module.sales.vo.SalesOrderItemVO;
import com.fruit.warehouse.module.sales.vo.SalesOrderPageVO;
import java.util.List;

/**
 * 销售管理 模块服务接口。
 */
public interface SalesService extends IService<SalesOrder> {
    /**
     * 创建销售单草稿。
     */
    SalesOrder createOrder(SalesOrderCreateRequest request);

    /**
     * 提交销售单。
     */
    SalesOrder submit(Long orderId);

    /**
     * 审核销售单。
     */
    SalesOrder approve(Long orderId);

    /**
     * 按明细分批出库并扣减库存。
     */
    SalesOrder ship(Long orderId, SalesShipRequest request);

    /**
     * 分页查询销售单。
     */
    IPage<SalesOrderPageVO> pageList(SalesOrderPageQuery query);

    /**
     * 查询销售单明细。
     */
    List<SalesOrderItemVO> listItems(Long orderId);
}
