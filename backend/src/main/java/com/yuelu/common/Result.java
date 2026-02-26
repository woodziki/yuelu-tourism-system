package com.yuelu.common;

import lombok.Data;

/**
 * 统一返回结果封装类。
 *
 * <p>按照 PRD 要求，前后端交互统一使用 Result<T> 包装类。
 * 这样前端可以统一处理成功/失败状态，避免每个接口返回格式不一致。</p>
 *
 * @param <T> 返回数据的类型
 */
@Data
public class Result<T> {

    /**
     * 状态码：200 表示成功，其他值表示失败。
     */
    private Integer code;

    /**
     * 提示信息（成功时可为空，失败时说明原因）。
     */
    private String message;

    /**
     * 返回的数据（泛型，可以是任意类型）。
     */
    private T data;

    /**
     * 成功返回（无数据）。    
     *
     * @return Result 对象  
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功返回（带数据）。
     *
     * @param data 返回的数据
     * @return Result 对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 失败返回。
     *
     * @param message 错误信息
     * @return Result 对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 失败返回（自定义状态码）。
     *
     * @param code 状态码
     * @param message 错误信息
     * @return Result 对象
     */ 
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
