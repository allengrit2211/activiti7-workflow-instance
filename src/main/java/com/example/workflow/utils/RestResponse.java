package com.example.workflow.utils;

public class RestResponse<T> {
    private int code;
    private String message;
    private T data;

    private RestResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static RestResponse successResult() {
        return RestResponse.successResult(200, "成功", null);
    }

    public static RestResponse successResult(Object data) {
        return RestResponse.successResult(200, "成功", data);
    }

    public static RestResponse successResult(int code, String message, Object data) {
        return new RestResponse(code, message, data);
    }

    public static RestResponse failResult() {
        return RestResponse.failResult(-100, "失败", null);
    }

    public static RestResponse failResult(int code, String message, Object data) {
        return new RestResponse(code, message, data);
    }

    private RestResponse() {
    }

    public int getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "RestResponse(code=" + this.getCode() + ", message=" + this.getMessage() + ", data=" + this.getData() + ")";
    }
}
