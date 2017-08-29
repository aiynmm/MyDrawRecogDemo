package com.sinosoft.mydrawrectdemo.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by Mars on 2017/6/28.
 */

public interface RecongnizeImgService {
    //http://124.16.139.218:8014/test/?image=
    //参数image： base64图片字符串
    @GET("test/")
    Call<ImgToWordEntity> getRecognizeWord(@Query("image") String base64Img);

}
