package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.dto.OutboundOrderCreateDTO;
import com.fruit.warehouse.vo.OutboundOrderVO;

public interface OutboundOrderService {

    Page<OutboundOrderVO> page(Integer pageNum, Integer pageSize, String orderCode, Integer status, Long warehouseId);

    OutboundOrderVO getById(Long id);

    Long create(OutboundOrderCreateDTO dto);

    void submit(Long id);

    void review(Long id, boolean approved, String remark);

    void pick(Long id);

    void complete(Long id);

    void cancel(Long id);
}
