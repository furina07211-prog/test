package com.fruit.warehouse.module.dashboard.mapper;

import com.fruit.warehouse.module.dashboard.dto.DailyPoint;
import com.fruit.warehouse.module.dashboard.dto.HeatmapPoint;
import com.fruit.warehouse.module.dashboard.dto.InventoryCategoryRatioPoint;
import com.fruit.warehouse.module.dashboard.dto.SalesTopPoint;
import com.fruit.warehouse.module.dashboard.dto.StockSummaryPoint;
import com.fruit.warehouse.module.dashboard.dto.WarningItemPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DashboardQueryMapper {

    @Select("""
        SELECT COUNT(DISTINCT fruit_id)
        FROM inventory_batch
        WHERE status = 'IN_STOCK'
          AND available_qty > 0
        """)
    Long countStockSku();

    @Select("""
        SELECT ROUND(COALESCE(SUM(total_amount), 0), 2)
        FROM purchase_order
        WHERE order_date = CURDATE()
          AND order_status <> 'DRAFT'
        """)
    BigDecimal sumTodayPurchaseAmount();

    @Select("""
        SELECT ROUND(COALESCE(SUM(total_amount), 0), 2)
        FROM sales_order
        WHERE DATE(order_time) = CURDATE()
          AND order_status IN ('SHIPPED', 'CONFIRMED')
        """)
    BigDecimal sumTodaySalesAmount();

    @Select("""
        SELECT COUNT(DISTINCT fruit_id)
        FROM inventory_alert
        WHERE alert_status = 'UNHANDLED'
        """)
    Long countUnhandledWarningFruits();

    @Select("""
        <script>
        SELECT order_date AS date,
               ROUND(COALESCE(SUM(total_amount), 0), 2) AS qty
        FROM purchase_order
        WHERE order_date <![CDATA[>=]]> #{startDate}
          AND order_date <![CDATA[<=]]> #{endDate}
          AND order_status <![CDATA[<>]]> 'DRAFT'
        GROUP BY order_date
        ORDER BY order_date
        </script>
        """)
    List<DailyPoint> selectPurchaseAmountTrend(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    @Select("""
        <script>
        SELECT DATE(order_time) AS date,
               ROUND(COALESCE(SUM(total_amount), 0), 2) AS qty
        FROM sales_order
        WHERE DATE(order_time) <![CDATA[>=]]> #{startDate}
          AND DATE(order_time) <![CDATA[<=]]> #{endDate}
          AND order_status IN ('SHIPPED', 'CONFIRMED')
        GROUP BY DATE(order_time)
        ORDER BY DATE(order_time)
        </script>
        """)
    List<DailyPoint> selectSalesAmountTrend(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Select("""
        SELECT fc.id AS categoryId,
               fc.category_name AS categoryName,
               ROUND(COALESCE(SUM(CASE WHEN ib.status = 'IN_STOCK' THEN ib.available_qty ELSE 0 END), 0), 2) AS stockQty
        FROM fruit_category fc
        LEFT JOIN fruit_info fi ON fi.category_id = fc.id AND fi.status = 1
        LEFT JOIN inventory_batch ib ON ib.fruit_id = fi.id AND ib.available_qty > 0
        GROUP BY fc.id, fc.category_name
        HAVING COALESCE(SUM(CASE WHEN ib.status = 'IN_STOCK' THEN ib.available_qty ELSE 0 END), 0) > 0
        ORDER BY stockQty DESC
        """)
    List<InventoryCategoryRatioPoint> selectInventoryCategoryRatio();

    @Select("""
        <script>
        SELECT soi.fruit_id AS fruitId,
               fi.fruit_name AS fruitName,
               ROUND(COALESCE(SUM(soi.quantity), 0), 2) AS salesQty,
               ROUND(COALESCE(SUM(soi.subtotal), 0), 2) AS salesAmount
        FROM sales_order so
        JOIN sales_order_item soi ON soi.sales_order_id = so.id
        LEFT JOIN fruit_info fi ON fi.id = soi.fruit_id
        WHERE DATE(so.order_time) <![CDATA[>=]]> #{startDate}
          AND DATE(so.order_time) <![CDATA[<=]]> #{endDate}
          AND so.order_status IN ('SHIPPED', 'CONFIRMED')
        GROUP BY soi.fruit_id, fi.fruit_name
        ORDER BY salesQty DESC, salesAmount DESC
        LIMIT #{limit}
        </script>
        """)
    List<SalesTopPoint> selectSalesTop(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("limit") Integer limit);

    @Select("""
        <script>
        SELECT ia.id AS alertId,
               ia.alert_type AS alertType,
               ia.alert_level AS alertLevel,
               ia.alert_msg AS alertMsg,
               ia.threshold_value AS thresholdValue,
               ia.current_value AS currentValue,
               ia.created_time AS createdTime,
               ia.fruit_id AS fruitId,
               fi.fruit_name AS fruitName,
               ia.warehouse_id AS warehouseId,
               wh.warehouse_name AS warehouseName,
               ia.batch_id AS batchId,
               ib.batch_no AS batchNo
        FROM inventory_alert ia
        LEFT JOIN fruit_info fi ON fi.id = ia.fruit_id
        LEFT JOIN warehouse wh ON wh.id = ia.warehouse_id
        LEFT JOIN inventory_batch ib ON ib.id = ia.batch_id
        WHERE ia.alert_status = 'UNHANDLED'
        ORDER BY ia.created_time DESC
        LIMIT #{limit}
        </script>
        """)
    List<WarningItemPoint> selectWarningItems(@Param("limit") Integer limit);

    @Select("""
        <script>
        SELECT DATE(so.order_time) AS date, ROUND(SUM(soi.quantity), 2) AS qty
        FROM sales_order so
        JOIN sales_order_item soi ON so.id = soi.sales_order_id
        WHERE so.order_time <![CDATA[>=]]> #{startDate}
          <if test='fruitId != null'>
            AND soi.fruit_id = #{fruitId}
          </if>
        GROUP BY DATE(so.order_time)
        ORDER BY DATE(so.order_time)
        </script>
        """)
    List<DailyPoint> selectDailySales(@Param("fruitId") Long fruitId, @Param("startDate") LocalDate startDate);

    @Select("""
        <script>
        SELECT fi.id AS fruitId,
               fi.fruit_name AS fruitName,
               #{warehouseId} AS warehouseId,
               ROUND(COALESCE(SUM(ib.available_qty), 0), 2) AS currentStockQty,
               ROUND(COALESCE(fi.safe_stock_qty, 0), 2) AS safeStockQty,
               0 AS inTransitQty
        FROM fruit_info fi
        LEFT JOIN inventory_batch ib ON ib.fruit_id = fi.id
             <if test='warehouseId != null'>
               AND ib.warehouse_id = #{warehouseId}
             </if>
        WHERE fi.status = 1
          <if test='fruitId != null'>
            AND fi.id = #{fruitId}
          </if>
        GROUP BY fi.id, fi.fruit_name, fi.safe_stock_qty
        ORDER BY fi.id
        </script>
        """)
    List<StockSummaryPoint> selectCurrentStock(@Param("fruitId") Long fruitId, @Param("warehouseId") Long warehouseId);

    @Select("""
        <script>
        SELECT poi.fruit_id AS fruitId,
               po.warehouse_id AS warehouseId,
               ROUND(SUM(GREATEST(poi.quantity - poi.received_qty, 0)), 2) AS inTransitQty
        FROM purchase_order po
        JOIN purchase_order_item poi ON poi.purchase_order_id = po.id
        WHERE po.order_status IN ('DRAFT','SUBMITTED','APPROVED')
          <if test='fruitId != null'>
            AND poi.fruit_id = #{fruitId}
          </if>
          <if test='warehouseId != null'>
            AND po.warehouse_id = #{warehouseId}
          </if>
        GROUP BY poi.fruit_id, po.warehouse_id
        </script>
        """)
    List<StockSummaryPoint> selectInTransit(@Param("fruitId") Long fruitId, @Param("warehouseId") Long warehouseId);

    @Select("""
        SELECT fi.fruit_name AS fruitName,
               ia.alert_type AS alertType,
               COUNT(1) AS alertCount
        FROM inventory_alert ia
        LEFT JOIN fruit_info fi ON fi.id = ia.fruit_id
        GROUP BY fi.fruit_name, ia.alert_type
        ORDER BY fi.fruit_name, ia.alert_type
        """)
    List<HeatmapPoint> selectAlertHeatmap();
}

