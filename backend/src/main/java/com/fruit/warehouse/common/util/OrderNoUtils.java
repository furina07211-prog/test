package com.fruit.warehouse.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoUtils() {
    }

    public static String generate(String prefix) {
        int random = ThreadLocalRandom.current().nextInt(100, 999);
        return prefix + LocalDateTime.now().format(FORMATTER) + random;
    }
}
