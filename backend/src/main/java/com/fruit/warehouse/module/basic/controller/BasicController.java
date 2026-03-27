package com.fruit.warehouse.module.basic.controller;

import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.common.result.Results;
import com.fruit.warehouse.module.basic.entity.Customer;
import com.fruit.warehouse.module.basic.entity.Fruit;
import com.fruit.warehouse.module.basic.entity.FruitCategory;
import com.fruit.warehouse.module.basic.entity.Supplier;
import com.fruit.warehouse.module.basic.entity.Warehouse;
import com.fruit.warehouse.module.basic.service.BasicService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 基础资料 模块控制器。
 */
@RestController
@RequestMapping("/api/basic")
@RequiredArgsConstructor
public class BasicController {

    private final BasicService basicService;

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<?> categories() {
        return Results.ok(basicService.listCategories());
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> createCategory(@RequestBody FruitCategory category) {
        basicService.saveCategory(category);
        return Results.ok();
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody FruitCategory category) {
        category.setId(id);
        basicService.updateCategory(category);
        return Results.ok();
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        basicService.deleteCategory(id);
        return Results.ok();
    }

    @GetMapping("/fruits")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<?> fruits(@RequestParam(defaultValue = "1") long current,
                            @RequestParam(defaultValue = "10") long size,
                            @RequestParam(required = false) String keyword) {
        return Results.page(basicService.pageFruits(current, size, keyword));
    }

    @PostMapping("/fruits")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> createFruit(@RequestBody Fruit fruit) {
        basicService.saveFruit(fruit);
        return Results.ok();
    }

    @PutMapping("/fruits/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> updateFruit(@PathVariable Long id, @RequestBody Fruit fruit) {
        fruit.setId(id);
        basicService.updateFruit(fruit);
        return Results.ok();
    }

    @DeleteMapping("/fruits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteFruit(@PathVariable Long id) {
        basicService.deleteFruit(id);
        return Results.ok();
    }

    @GetMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<?> suppliers(@RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "10") long size,
                               @RequestParam(required = false) String keyword) {
        return Results.page(basicService.pageSuppliers(current, size, keyword));
    }

    @PostMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> createSupplier(@RequestBody Supplier supplier) {
        basicService.saveSupplier(supplier);
        return Results.ok();
    }

    @PutMapping("/suppliers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        supplier.setId(id);
        basicService.updateSupplier(supplier);
        return Results.ok();
    }

    @DeleteMapping("/suppliers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteSupplier(@PathVariable Long id) {
        basicService.deleteSupplier(id);
        return Results.ok();
    }

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public Result<?> customers(@RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "10") long size,
                               @RequestParam(required = false) String keyword) {
        return Results.page(basicService.pageCustomers(current, size, keyword));
    }

    @PostMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public Result<Void> createCustomer(@RequestBody Customer customer) {
        basicService.saveCustomer(customer);
        return Results.ok();
    }

    @PutMapping("/customers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SALES')")
    public Result<Void> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        basicService.updateCustomer(customer);
        return Results.ok();
    }

    @DeleteMapping("/customers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        basicService.deleteCustomer(id);
        return Results.ok();
    }

    @GetMapping("/warehouses")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE','SALES')")
    public Result<?> warehouses() {
        return Results.ok(basicService.listWarehouses());
    }

    @PostMapping("/warehouses")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> createWarehouse(@RequestBody Warehouse warehouse) {
        basicService.saveWarehouse(warehouse);
        return Results.ok();
    }

    @PutMapping("/warehouses/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','WAREHOUSE')")
    public Result<Void> updateWarehouse(@PathVariable Long id, @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        basicService.updateWarehouse(warehouse);
        return Results.ok();
    }

    @DeleteMapping("/warehouses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteWarehouse(@PathVariable Long id) {
        basicService.deleteWarehouse(id);
        return Results.ok();
    }
}
