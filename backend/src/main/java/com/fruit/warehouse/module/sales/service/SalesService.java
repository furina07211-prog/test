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

public interface SalesService extends IService<SalesOrder> {
    SalesOrder createOrder(SalesOrderCreateRequest request);

    SalesOrder submit(Long orderId);

    SalesOrder approve(Long orderId);

    SalesOrder ship(Long orderId, SalesShipRequest request);

    IPage<SalesOrderPageVO> pageList(SalesOrderPageQuery query);

    List<SalesOrderItemVO> listItems(Long orderId);
}
