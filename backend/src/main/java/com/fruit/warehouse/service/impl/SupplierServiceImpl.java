package com.fruit.warehouse.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.common.BusinessException;
import com.fruit.warehouse.entity.Supplier;
import com.fruit.warehouse.mapper.SupplierMapper;
import com.fruit.warehouse.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierMapper supplierMapper;

    @Override
    public Page<Supplier> page(Integer pageNum, Integer pageSize, String supplierName) {
        Page<Supplier> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(supplierName), Supplier::getSupplierName, supplierName)
               .orderByDesc(Supplier::getCreateTime);
        return supplierMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Supplier> listAll() {
        return supplierMapper.selectList(new LambdaQueryWrapper<Supplier>()
                .eq(Supplier::getStatus, 1)
                .orderByAsc(Supplier::getSupplierCode));
    }

    @Override
    public Supplier getById(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public void create(Supplier supplier) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Supplier::getSupplierCode, supplier.getSupplierCode());
        if (supplierMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("供应商编码已存在");
        }
        supplier.setStatus(1);
        supplierMapper.insert(supplier);
    }

    @Override
    public void update(Supplier supplier) {
        Supplier existing = supplierMapper.selectById(supplier.getId());
        if (existing == null) {
            throw new BusinessException("供应商不存在");
        }
        supplierMapper.updateById(supplier);
    }

    @Override
    public void delete(Long id) {
        supplierMapper.deleteById(id);
    }
}
