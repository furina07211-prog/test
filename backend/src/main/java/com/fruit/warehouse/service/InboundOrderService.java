package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.dto.InboundOrderCreateDTO;
import com.fruit.warehouse.dto.InboundReceiveDTO;
import com.fruit.warehouse.vo.InboundOrderVO;

public interface InboundOrderService {

    Page<InboundOrderVO> page(Integer pageNum, Integer pageSize, String orderCode, Integer status, Long supplierId, Long warehouseId);

    InboundOrderVO getById(Long id);

    Long create(InboundOrderCreateDTO dto);

    void submit(Long id);

    void review(Long id, boolean approved, String remark);

    void receive(InboundReceiveDTO dto);

    void complete(Long id);

    void cancel(Long id);
}
