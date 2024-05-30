package com.ut.killer.http.response;

public class ResultData<T> {
    private String code;
    private String msg;
    private T data;

    public static <T> ResultData<T> success(T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setData(data);
        resultData.setCode("0");
        resultData.setMsg("success");
        return resultData;
    }

    public static ResultData<String> error(String code, String msg) {
        ResultData<String> resultData = new ResultData<>();
        resultData.setCode(code);
        resultData.setMsg(msg);
        return resultData;
    }

    public static String error(String msg) {
        return "{\"code\":\"1\",\"msg\":\"" + msg + "\",\"data\":\"\"}";
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
