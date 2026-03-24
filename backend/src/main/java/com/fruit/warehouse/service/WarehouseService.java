package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.entity.Warehouse;

import java.util.List;

public interface WarehouseService {

    Page<Warehouse> page(Integer pageNum, Integer pageSize, String warehouseName);

    List<Warehouse> listAll();

    Warehouse getById(Long id);

    void create(Warehouse warehouse);

    void update(Warehouse warehouse);

    void delete(Long id);
}
