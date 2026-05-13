package com.youdao.paper.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultVO<T> {

    private String code;
    private String msg;
    private T data;

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> ResultVO<T> success(String msg, T data) {
        return new ResultVO<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ResultVO<T> fail(ResultCode resultCode) {
        return new ResultVO<>(resultCode.getCode(), resultCode.getMsg(), null);
    }

    public static <T> ResultVO<T> fail(String code, String msg) {
        return new ResultVO<>(code, msg, null);
    }
}
