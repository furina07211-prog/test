package com.fruit.warehouse.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;

public final class Results {
    private Results() {
    }

    /**
     * 返回成功结果（含数据）。
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    /**
     * 返回成功结果（无数据）。
     */
    public static Result<Void> ok() {
        return new Result<>(200, "success", null);
    }

    /**
     * 返回失败结果。
     */
    public static Result<Void> fail(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 将 MyBatis 分页对象转换为统一分页返回。
     */
    public static <T> Result<PageResult<T>> page(IPage<T> page) {
        PageResult<T> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return ok(result);
    }
}
