package com.fruit.warehouse.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fruit.warehouse.entity.*;
import com.fruit.warehouse.mapper.*;
import com.fruit.warehouse.service.InventoryService;
import com.fruit.warehouse.vo.BatchVO;
import com.fruit.warehouse.vo.InventoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryBatchMapper batchMapper;
    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final SupplierMapper supplierMapper;

    @Override
    public Page<InventoryVO> page(Integer pageNum, Integer pageSize, String productName, Long warehouseId, Long categoryId) {
        Page<Inventory> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
               .orderByDesc(Inventory::getUpdateTime);

        // If productName or categoryId is provided, we need to filter by product
        if (StrUtil.isNotBlank(productName) || categoryId != null) {
            LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
            productWrapper.like(StrUtil.isNotBlank(productName), Product::getProductName, productName)
                          .eq(categoryId != null, Product::getCategoryId, categoryId);
            List<Product> products = productMapper.selectList(productWrapper);
            if (products.isEmpty()) {
                return new Page<>(pageNum, pageSize, 0);
            }
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            wrapper.in(Inventory::getProductId, productIds);
        }

        Page<Inventory> result = inventoryMapper.selectPage(page, wrapper);
        Page<InventoryVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<BatchVO> pageBatches(Integer pageNum, Integer pageSize, Long productId, Long warehouseId, Integer status) {
        Page<InventoryBatch> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<InventoryBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(productId != null, InventoryBatch::getProductId, productId)
               .eq(warehouseId != null, InventoryBatch::getWarehouseId, warehouseId)
               .eq(status != null, InventoryBatch::getBatchStatus, status)
               .orderByAsc(InventoryBatch::getExpiryDate);

        Page<InventoryBatch> result = batchMapper.selectPage(page, wrapper);
        Page<BatchVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toBatchVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public InventoryVO getByProductAndWarehouse(Long productId, Long warehouseId) {
        Inventory inventory = inventoryMapper.selectByProductAndWarehouse(productId, warehouseId);
        if (inventory == null) {
            return null;
        }
        InventoryVO vo = toVO(inventory);

        // Load batches
        List<InventoryBatch> batches = batchMapper.selectList(
                new LambdaQueryWrapper<InventoryBatch>()
                        .eq(InventoryBatch::getProductId, productId)
                        .eq(InventoryBatch::getWarehouseId, warehouseId)
                        .gt(InventoryBatch::getRemainingQuantity, 0)
                        .orderByAsc(InventoryBatch::getExpiryDate));
        vo.setBatches(batches.stream().map(this::toBatchVO).collect(Collectors.toList()));

        return vo;
    }

    private InventoryVO toVO(Inventory inventory) {
        InventoryVO vo = new InventoryVO();
        vo.setId(inventory.getId());
        vo.setProductId(inventory.getProductId());
        vo.setWarehouseId(inventory.getWarehouseId());
        vo.setTotalQuantity(inventory.getTotalQuantity());
        vo.setLockedQuantity(inventory.getLockedQuantity());
        vo.setAvailableQuantity(inventory.getAvailableQuantity());
        vo.setUpdateTime(inventory.getUpdateTime());

        Product product = productMapper.selectById(inventory.getProductId());
        if (product != null) {
            vo.setProductCode(product.getProductCode());
            vo.setProductName(product.getProductName());
            vo.setUnit(product.getUnit());
            vo.setLowStockThreshold(product.getLowStockThreshold());

            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }

            // Determine stock status
            if (inventory.getAvailableQuantity().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                vo.setStockStatus("缺货");
            } else if (inventory.getAvailableQuantity().intValue() < product.getLowStockThreshold()) {
                vo.setStockStatus("库存不足");
            } else {
                vo.setStockStatus("正常");
            }
        }

        Warehouse warehouse = warehouseMapper.selectById(inventory.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        return vo;
    }

    private BatchVO toBatchVO(InventoryBatch batch) {
        BatchVO vo = new BatchVO();
        vo.setId(batch.getId());
        vo.setBatchCode(batch.getBatchCode());
        vo.setProductId(batch.getProductId());
        vo.setWarehouseId(batch.getWarehouseId());
        vo.setSupplierId(batch.getSupplierId());
        vo.setQuantity(batch.getQuantity());
        vo.setRemainingQuantity(batch.getRemainingQuantity());
        vo.setUnitCost(batch.getUnitCost());
        vo.setProductionDate(batch.getProductionDate());
        vo.setExpiryDate(batch.getExpiryDate());
        vo.setBatchStatus(batch.getBatchStatus());
        vo.setCreateTime(batch.getCreateTime());

        // Calculate remaining days
        if (batch.getExpiryDate() != null) {
            long days = DateUtil.betweenDay(new Date(), batch.getExpiryDate(), false);
            vo.setRemainingDays((int) days);
        }

        // Status description
        switch (batch.getBatchStatus()) {
            case 1: vo.setBatchStatusDesc("正常"); break;
            case 2: vo.setBatchStatusDesc("即将过期"); break;
            case 3: vo.setBatchStatusDesc("已过期"); break;
            case 4: vo.setBatchStatusDesc("已耗尽"); break;
            default: vo.setBatchStatusDesc("未知");
        }

        Product product = productMapper.selectById(batch.getProductId());
        if (product != null) {
            vo.setProductName(product.getProductName());
        }

        Warehouse warehouse = warehouseMapper.selectById(batch.getWarehouseId());
        if (warehouse != null) {
            vo.setWarehouseName(warehouse.getWarehouseName());
        }

        if (batch.getSupplierId() != null) {
            Supplier supplier = supplierMapper.selectById(batch.getSupplierId());
            if (supplier != null) {
                vo.setSupplierName(supplier.getSupplierName());
            }
        }

        return vo;
    }
}
