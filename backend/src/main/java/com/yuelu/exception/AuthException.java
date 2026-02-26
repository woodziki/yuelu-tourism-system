package com.yuelu.exception;

/**
 * 认证异常：Token 无效或未携带时抛出。
 *
 * <p>由全局异常处理器捕获后统一返回 Result.error("请先登录")，便于前端跳转登录页。</p>
 */
public class AuthException extends RuntimeException {

    public AuthException() {
        super("请先登录");
    }

    public AuthException(String message) {
        super(message);
    }
}
