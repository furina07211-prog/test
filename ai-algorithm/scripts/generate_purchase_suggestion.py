def generate_purchase_suggestion(predicted_daily_qty, lead_time_days, safe_stock_qty, current_stock_qty, in_transit_qty):
    predicted_daily_qty = float(predicted_daily_qty or 0)
    lead_time_days = int(lead_time_days or 1)
    safe_stock_qty = float(safe_stock_qty or 0)
    current_stock_qty = float(current_stock_qty or 0)
    in_transit_qty = float(in_transit_qty or 0)

    required = predicted_daily_qty * lead_time_days + safe_stock_qty
    recommended = max(0.0, required - current_stock_qty - in_transit_qty)
    return {
        'predictedDailyQty': round(predicted_daily_qty, 2),
        'leadTimeDays': lead_time_days,
        'safetyStockQty': round(safe_stock_qty, 2),
        'currentStockQty': round(current_stock_qty, 2),
        'inTransitQty': round(in_transit_qty, 2),
        'recommendedPurchaseQty': round(recommended, 2)
    }
