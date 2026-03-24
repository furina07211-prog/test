package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.entity.Supplier;

import java.util.List;

public interface SupplierService {

    Page<Supplier> page(Integer pageNum, Integer pageSize, String supplierName);

    List<Supplier> listAll();

    Supplier getById(Long id);

    void create(Supplier supplier);

    void update(Supplier supplier);

    void delete(Long id);
}
