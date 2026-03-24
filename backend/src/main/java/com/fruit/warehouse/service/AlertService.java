package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.entity.Alert;

public interface AlertService {

    Page<Alert> page(Integer pageNum, Integer pageSize, Integer alertType, Integer status);

    int countUnread();

    void markRead(Long id);

    void handle(Long id, String remark);
}
