package com.fruit.warehouse.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.entity.Product;

import java.util.List;

public interface ProductService {

    Page<Product> page(Integer pageNum, Integer pageSize, String productName, Long categoryId, Integer status);

    List<Product> listAll();

    Product getById(Long id);

    void create(Product product);

    void update(Product product);

    void delete(Long id);
}
