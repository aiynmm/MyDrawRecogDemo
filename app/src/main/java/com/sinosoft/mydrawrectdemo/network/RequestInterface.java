package com.sinosoft.mydrawrectdemo.network;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Admin on 2016/7/19.
 */
public interface RequestInterface {
    @GET("attribute/")
    Flowable<ResponseData> getResponse(@Query("midNo") String midNo);
}
