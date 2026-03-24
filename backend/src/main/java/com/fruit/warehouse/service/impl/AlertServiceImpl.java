package com.fruit.warehouse.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.entity.Alert;
import com.fruit.warehouse.mapper.AlertMapper;
import com.fruit.warehouse.security.UserContext;
import com.fruit.warehouse.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertMapper alertMapper;

    @Override
    public Page<Alert> page(Integer pageNum, Integer pageSize, Integer alertType, Integer status) {
        Page<Alert> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Alert> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(alertType != null, Alert::getAlertType, alertType)
               .eq(status != null, Alert::getStatus, status)
               .orderByDesc(Alert::getCreateTime);
        return alertMapper.selectPage(page, wrapper);
    }

    @Override
    public int countUnread() {
        return alertMapper.countUnread();
    }

    @Override
    public void markRead(Long id) {
        Alert alert = alertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException("预警不存在");
        }
        if (alert.getStatus() == 0) {
            alert.setStatus(1);
            alertMapper.updateById(alert);
        }
    }

    @Override
    public void handle(Long id, String remark) {
        Alert alert = alertMapper.selectById(id);
        if (alert == null) {
            throw new BusinessException("预警不存在");
        }
        alert.setStatus(2);
        alert.setHandlerId(UserContext.getUserId());
        alert.setHandleTime(new Date());
        alert.setHandleRemark(remark);
        alertMapper.updateById(alert);
    }
}
