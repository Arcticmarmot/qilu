package com.marmot.qilu.common.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T data;

    public ApiResponse() {}

    public ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(){
        return new ApiResponse<>(0, "success", null);
    }

    public static <T> ApiResponse<T> success(T data){
        return new ApiResponse<>(0, "success", data);
    }

    public static <T> ApiResponse<T> fail(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

}
