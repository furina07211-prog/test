package com.fruit.warehouse.common.constant;

public final class BusinessConstants {

    public static final String PURCHASE_DRAFT = "DRAFT";
    public static final String PURCHASE_RECEIVED = "RECEIVED";
    public static final String SALES_DRAFT = "DRAFT";
    public static final String SALES_CONFIRMED = "CONFIRMED";
    public static final String CHECK_DRAFT = "DRAFT";
    public static final String CHECK_APPROVED = "APPROVED";
    public static final String INVENTORY_INBOUND = "PURCHASE_IN";
    public static final String INVENTORY_OUTBOUND = "SALES_OUT";
    public static final String INVENTORY_CHECK = "STOCK_CHECK";
    public static final String ALERT_LOW_STOCK = "LOW_STOCK";
    public static final String ALERT_EXPIRING = "EXPIRING";
    public static final String ALERT_UNHANDLED = "UNHANDLED";

    private BusinessConstants() {
    }
}
