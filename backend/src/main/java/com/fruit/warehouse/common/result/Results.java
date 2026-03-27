package com.fruit.warehouse.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;

public final class Results {
    private Results() {}

    public static <T> Result<T> ok(T data) {
        return new Result<>(200, "success", data);
    }

    public static Result<Void> ok() {
        return new Result<>(200, "success", null);
    }

    public static Result<Void> fail(String message) {
        return new Result<>(500, message, null);
    }

    public static <T> Result<PageResult<T>> page(IPage<T> page) {
        PageResult<T> result = new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
        return ok(result);
    }
}
