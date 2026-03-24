package com.fruit.warehouse.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.entity.Warehouse;
import com.fruit.warehouse.mapper.WarehouseMapper;
import com.fruit.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseMapper warehouseMapper;

    @Override
    public Page<Warehouse> page(Integer pageNum, Integer pageSize, String warehouseName) {
        Page<Warehouse> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(warehouseName), Warehouse::getWarehouseName, warehouseName)
               .orderByDesc(Warehouse::getCreateTime);
        return warehouseMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Warehouse> listAll() {
        return warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getStatus, 1)
                .orderByAsc(Warehouse::getWarehouseCode));
    }

    @Override
    public Warehouse getById(Long id) {
        return warehouseMapper.selectById(id);
    }

    @Override
    public void create(Warehouse warehouse) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Warehouse::getWarehouseCode, warehouse.getWarehouseCode());
        if (warehouseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("仓库编码已存在");
        }
        warehouse.setStatus(1);
        warehouseMapper.insert(warehouse);
    }

    @Override
    public void update(Warehouse warehouse) {
        Warehouse existing = warehouseMapper.selectById(warehouse.getId());
        if (existing == null) {
            throw new BusinessException("仓库不存在");
        }
        warehouseMapper.updateById(warehouse);
    }

    @Override
    public void delete(Long id) {
        warehouseMapper.deleteById(id);
    }
}
