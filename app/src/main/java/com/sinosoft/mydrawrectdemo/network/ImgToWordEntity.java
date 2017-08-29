package com.sinosoft.mydrawrectdemo.network;

/**
 * Created by Mars on 2017/6/28.
 */

public class ImgToWordEntity {

    /**
     * codes : 丁,了,于
     * error_code : 0
     * probability : [ 0.94199586 0.04819471 0.00592718]
     */

    private String codes;
    private String error_code;
    private String probability;

    public String getCodes() {
        return codes;
    }

    public void setCodes(String codes) {
        this.codes = codes;
    }

    public String getError_code() {
        return error_code;
    }

    public void setError_code(String error_code) {
        this.error_code = error_code;
    }

    public String getProbability() {
        return probability;
    }

    public void setProbability(String probability) {
        this.probability = probability;
    }
}
