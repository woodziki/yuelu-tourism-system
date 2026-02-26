package com.yuelu.handler;

import com.yuelu.common.Result;
import com.yuelu.exception.AuthException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器。
 *
 * <p>捕获 AuthException 与非法参数等，统一返回 Result，避免把异常栈抛给前端。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Token 无效或未登录时，拦截器抛出 AuthException，这里统一返回「请先登录」。
     */
    @ExceptionHandler(AuthException.class)
    public Result<?> handleAuth(AuthException e) {
        return Result.error(401, e.getMessage() != null ? e.getMessage() : "请先登录");
    }

    /**
     * 参数校验失败（如注册/登录参数为空、用户名已存在、密码错误等）。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 其他未预期异常，统一返回 500，避免泄露内部信息。
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleOther(Exception e) {
        return Result.error("服务器繁忙，请稍后重试");
    }
}
