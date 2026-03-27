package com.fruit.warehouse.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    /**
     * 兼容旧代码的成功返回工厂方法，内部委托给 {@link Results}。
     */
    public static <T> Result<T> success(T data) {
        return Results.ok(data);
    }

    /**
     * 兼容旧代码的空成功返回。
     */
    public static Result<Void> success() {
        return Results.ok();
    }

    /**
     * 兼容旧代码的失败返回。
     */
    public static Result<Void> fail(String message) {
        return Results.fail(message);
    }
}
