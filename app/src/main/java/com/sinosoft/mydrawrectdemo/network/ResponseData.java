package com.sinosoft.mydrawrectdemo.network;

import java.util.List;

/**
 * Created by Admin on 2016/7/19.
 */
public class ResponseData {
    private String result;
    private List<Res> res;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Res> getRes() {
        return res;
    }

    public void setRes(List<Res> res) {
        this.res = res;
    }
}
