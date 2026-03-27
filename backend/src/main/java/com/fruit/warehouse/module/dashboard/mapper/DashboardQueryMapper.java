package com.fruit.warehouse.module.dashboard.mapper;

import com.fruit.warehouse.module.dashboard.dto.DailyPoint;
import com.fruit.warehouse.module.dashboard.dto.HeatmapPoint;
import com.fruit.warehouse.module.dashboard.dto.StockSummaryPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DashboardQueryMapper {

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