package com.fruit.warehouse.common.exception;

import com.fruit.warehouse.common.result.Result;
import com.fruit.warehouse.common.result.Results;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，统一输出接口错误结构与可读提示。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        return new Result<>(400, safeMessage(e.getMessage(), "业务处理失败"), null);
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        ConstraintViolationException.class,
        BindException.class,
        HttpMessageNotReadableException.class
    })
    public Result<Void> handleValidation(Exception e) {
        return new Result<>(400, extractValidationMessage(e), null);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        return Results.fail(safeMessage(e.getMessage(), "系统异常，请稍后重试"));
    }

    private String extractValidationMessage(Exception e) {
        if (e instanceof MethodArgumentNotValidException ex && ex.getBindingResult().hasFieldErrors()) {
            return ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        if (e instanceof BindException ex && ex.getBindingResult().hasFieldErrors()) {
            return ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        if (e instanceof ConstraintViolationException ex && !ex.getConstraintViolations().isEmpty()) {
            return ex.getConstraintViolations().iterator().next().getMessage();
        }
        if (e instanceof HttpMessageNotReadableException) {
            return "请求参数格式错误";
        }
        return safeMessage(e.getMessage(), "请求参数校验失败");
    }

    private String safeMessage(String message, String fallback) {
        return (message == null || message.isBlank()) ? fallback : message;
    }
}
