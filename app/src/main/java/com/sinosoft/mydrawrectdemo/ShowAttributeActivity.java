package com.sinosoft.mydrawrectdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.texttype.MidPlaneTextTool;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.sinosoft.mydrawrectdemo.network.RequestInterface;
import com.sinosoft.mydrawrectdemo.network.Res;
import com.sinosoft.mydrawrectdemo.network.ResponseData;


import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Admin on 2016/12/5.
 */

public class ShowAttributeActivity extends AppCompatActivity {
    //http://124.16.139.218/attribute/?midNo=0623A3
    private static final String BASE_URL = "http://124.16.139.218/";
    private Toolbar toolbar;
    private ProgressDialog progressDialog;

    @BindView(R.id.ExternalNo)
    TextView ExternalNo;//外字编码：16ZH:5120
    @BindView(R.id.MidNo)
    TextView MidNo;//中间字编码：0623A3
    @BindView(R.id.CharType)
    TextView CharType;//字符,需中间字支持
    @BindView(R.id.IDS)
    TextView IDS;//IDS，需中间字支持
    @BindView(R.id.ImageName)
    TextView ImageName;//所在图像文件名：ZHXHP001061-000083
    @BindView(R.id.TitileName)
    TextView TitileName;//正题名：中國倫理政治大綱
    @BindView(R.id.StokeNum)
    TextView StokeNum;//总笔画数：32
    @BindView(R.id.StokeOrder)
    TextView StokeOrder;//全笔顺：25121252511251212512125251125121
    @BindView(R.id.StandardRadical)
    TextView StandardRadical;//规范主部首
    @BindView(R.id.AttachRadical)
    TextView AttachRadical;//规范附型部首，需中间字支持
    @BindView(R.id.AttachRadicalNum)
    TextView AttachRadicalNum;//规范部首外笔画数：14
    @BindView(R.id.AttachRadicalOrder)
    TextView AttachRadicalOrder;//规范部首外笔顺：252511251212512125251125121
    @BindView(R.id.KangRadical)
    TextView KangRadical;//康熙主部首
    @BindView(R.id.KangAttachRadical)
    TextView KangAttachRadical;//康熙附型部首，需中间字支持
    @BindView(R.id.KangAttachRadicalNum)
    TextView KangAttachRadicalNum;//康熙部首外笔画数：14
    @BindView(R.id.KangAttachRadicalOrder)
    TextView KangAttachRadicalOrder;//康熙部首外笔顺：252511251212512125251125121
    @BindView(R.id.Pinyin)
    TextView Pinyin;//汉语拼音：bin4
    @BindView(R.id.Paraphrase)
    TextView Paraphrase;//释义："同“儐”，陳列。需中间字支持
    @BindView(R.id.RelatedWord)
    TextView RelatedWord;//关系字：儐。需中间字支持
    @BindView(R.id.Relations)
    TextView Relations;//字际关系
    @BindView(R.id.Examples)
    TextView Examples;//例证：xxxxxxxxx。需中间字支持
    @BindView(R.id.ExampleFrom)
    TextView ExampleFrom;

    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_show_attr);
        unbinder = ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar_show);
        //关于toolbar的设置必须在setSupportActionBar()调用前
        //setTitle()或者在manifest中设置activity的label效果是一样的
        toolbar.setTitle("");//这里是让title居中
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        initDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String midNo = getIntent().getStringExtra("midNo");
        getResponseData(midNo);
    }

    private void initDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(true);
        progressDialog.setMessage("正在请求加载属性信息，请稍候");
        progressDialog.show();
    }

    private void getResponseData(String midNo) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//1.X为RxJavaCallAdapterFactory
                .build();

        RequestInterface requestInterface = retrofit.create(RequestInterface.class);
        requestInterface.getResponse(midNo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResponseData>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(ResponseData responseData) {
                        setDataToUi(responseData.getRes().get(0));
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e("onError",t.getMessage());
                        progressDialog.dismiss();
                        Snackbar.make(toolbar, "出错了，请确保网络连接正常，稍候重试！", Snackbar.LENGTH_LONG)
                                .setAction("返回", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        finish();
                                    }
                                }).show();

                    }

                    @Override
                    public void onComplete() {
                        //只有执行成功，才会回调此方法
                        progressDialog.dismiss();
                    }
                });
    }

    private void setDataToUi(Res res) {
        setNotNullData(ExternalNo, res.getExternalNo());
        setNotNullData(MidNo, res.getMidNo());
        setNotNullMidData(CharType, res.getCharType());
        setNotNullMidData(IDS, res.getIDS());
        setNotNullData(ImageName, res.getImageName());
        setNotNullData(TitileName, res.getTitileName());
        setNotNullData(StokeNum, res.getStokeNum());
        setNotNullData(StokeOrder, res.getStokeOrder());
        setNotNullData(StandardRadical, res.getStandardRadical());
        setNotNullMidData(AttachRadical, res.getAttachRadical());
        setNotNullData(AttachRadicalNum, res.getAttachRadicalNum());
        setNotNullData(AttachRadicalOrder, res.getAttachRadicalOrder());
        setNotNullData(KangRadical, res.getKangRadical());
        setNotNullMidData(KangAttachRadical, res.getKangAttachRadical());
        setNotNullData(KangAttachRadicalNum, res.getKangAttachRadicalNum());
        setNotNullData(KangAttachRadicalOrder, res.getKangAttachRadicalOrder());
        setNotNullData(Pinyin, res.getPinyin());
        setNotNullMidData(RelatedWord, res.getRelatedWord());
        setNotNullMidData(Paraphrase, res.getParaphrase());
        setNotNullData(Relations, res.getRelations());
        setNotNullMidData(Examples, res.getExamples());
        setNotNullMidData(ExampleFrom, res.getExampleFrom());
    }

    private void setNotNullData(TextView textView, String content) {
        if ("".equals(content)) {
            textView.setText("无");
        } else {
            textView.setText(content);
        }
    }

    private void setNotNullMidData(TextView textView, String content) {
        if ("".equals(content)) {
            textView.setText("无");
        } else {
            MidPlaneTextTool tool=new MidPlaneTextTool(content,this,textView);
            tool.handleFont();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
