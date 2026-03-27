package com.fruit.warehouse.module.basic.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.module.basic.entity.Customer;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.entity.FruitCategory;
import com.fruit.warehouse.module.basic.entity.Supplier;
import com.fruit.warehouse.module.basic.entity.Warehouse;

import java.util.List;

public interface BasicService {
    List<FruitCategory> listCategories();
    void saveCategory(FruitCategory category);
    void updateCategory(FruitCategory category);
    void deleteCategory(Long id);

    Page<Fruit> pageFruits(long current, long size, String keyword);
    void saveFruit(Fruit fruit);
    void updateFruit(Fruit fruit);
    void deleteFruit(Long id);

    Page<Supplier> pageSuppliers(long current, long size, String keyword);
    void saveSupplier(Supplier supplier);
    void updateSupplier(Supplier supplier);
    void deleteSupplier(Long id);

    Page<Customer> pageCustomers(long current, long size, String keyword);
    void saveCustomer(Customer customer);
    void updateCustomer(Customer customer);
    void deleteCustomer(Long id);

    List<Warehouse> listWarehouses();
    void saveWarehouse(Warehouse warehouse);
    void updateWarehouse(Warehouse warehouse);
    void deleteWarehouse(Long id);
}
