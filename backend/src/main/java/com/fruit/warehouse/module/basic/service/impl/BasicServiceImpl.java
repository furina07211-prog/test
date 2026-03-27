package com.fruit.warehouse.module.basic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.module.basic.entity.Customer;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.entity.FruitCategory;
import com.fruit.warehouse.module.basic.entity.Supplier;
import com.fruit.warehouse.module.basic.entity.Warehouse;
import com.fruit.warehouse.module.basic.mapper.CustomerMapper;
import com.fruit.warehouse.module.basic.mapper.FruitCategoryMapper;
import com.fruit.warehouse.module.basic.mapper.FruitMapper;
import com.fruit.warehouse.module.basic.mapper.SupplierMapper;
import com.fruit.warehouse.module.basic.mapper.WarehouseMapper;
import com.fruit.warehouse.module.basic.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 基础资料 模块服务实现。
 */
@Service
@RequiredArgsConstructor
public class BasicServiceImpl implements BasicService {

    private final FruitMapper fruitMapper;
    private final SupplierMapper supplierMapper;
    private final CustomerMapper customerMapper;
    private final FruitCategoryMapper fruitCategoryMapper;
    private final WarehouseMapper warehouseMapper;

    @Override
    public List<FruitCategory> listCategories() {
        return fruitCategoryMapper.selectList(new LambdaQueryWrapper<FruitCategory>()
            .orderByAsc(FruitCategory::getSortNo)
            .orderByAsc(FruitCategory::getId));
    }

    @Override
    public void saveCategory(FruitCategory category) {
        fruitCategoryMapper.insert(category);
    }

    @Override
    public void updateCategory(FruitCategory category) {
        fruitCategoryMapper.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        fruitCategoryMapper.deleteById(id);
    }

    @Override
    public Page<Fruit> pageFruits(long current, long size, String keyword) {
        LambdaQueryWrapper<Fruit> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(keyword), Fruit::getFruitName, keyword)
            .or(StringUtils.isNotBlank(keyword))
            .like(StringUtils.isNotBlank(keyword), Fruit::getFruitCode, keyword)
            .orderByDesc(Fruit::getCreateTime);
        return fruitMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public void saveFruit(Fruit fruit) {
        fruitMapper.insert(fruit);
    }

    @Override
    public void updateFruit(Fruit fruit) {
        fruitMapper.updateById(fruit);
    }

    @Override
    public void deleteFruit(Long id) {
        fruitMapper.deleteById(id);
    }

    @Override
    public Page<Supplier> pageSuppliers(long current, long size, String keyword) {
        LambdaQueryWrapper<Supplier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(keyword), Supplier::getSupplierName, keyword)
            .or(StringUtils.isNotBlank(keyword))
            .like(StringUtils.isNotBlank(keyword), Supplier::getSupplierCode, keyword)
            .orderByDesc(Supplier::getCreateTime);
        return supplierMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public void saveSupplier(Supplier supplier) {
        supplierMapper.insert(supplier);
    }

    @Override
    public void updateSupplier(Supplier supplier) {
        supplierMapper.updateById(supplier);
    }

    @Override
    public void deleteSupplier(Long id) {
        supplierMapper.deleteById(id);
    }

    @Override
    public Page<Customer> pageCustomers(long current, long size, String keyword) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(keyword), Customer::getCustomerName, keyword)
            .or(StringUtils.isNotBlank(keyword))
            .like(StringUtils.isNotBlank(keyword), Customer::getCustomerCode, keyword)
            .orderByDesc(Customer::getCreateTime);
        return customerMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public void saveCustomer(Customer customer) {
        customerMapper.insert(customer);
    }

    @Override
    public void updateCustomer(Customer customer) {
        customerMapper.updateById(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerMapper.deleteById(id);
    }

    @Override
    public List<Warehouse> listWarehouses() {
        return warehouseMapper.selectList(new LambdaQueryWrapper<Warehouse>()
            .orderByAsc(Warehouse::getId));
    }

    @Override
    public void saveWarehouse(Warehouse warehouse) {
        warehouseMapper.insert(warehouse);
    }

    @Override
    public void updateWarehouse(Warehouse warehouse) {
        warehouseMapper.updateById(warehouse);
    }

    @Override
    public void deleteWarehouse(Long id) {
        warehouseMapper.deleteById(id);
    }
}
