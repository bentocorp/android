package com.bentonow.bentonow.model.socket;

/**
 * Created by kokusho on 3/7/16.
 */
public class ResponseSocketModel {
    private int code;
    private String msg;
    private String ret;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public String getRet() {
        return ret;
    }

    public void setRet(String ret) {
        this.ret = ret;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

}
