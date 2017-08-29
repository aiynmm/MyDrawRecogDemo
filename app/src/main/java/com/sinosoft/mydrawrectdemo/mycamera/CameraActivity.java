package com.sinosoft.mydrawrectdemo.mycamera;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sinosoft.mydrawrectdemo.DrawRectImgView;
import com.sinosoft.mydrawrectdemo.R;
import com.sinosoft.mydrawrectdemo.ShowAttributeActivity;

/**
 * Created by Mars on 2017/6/5.
 */

public class CameraActivity extends AppCompatActivity {
    private CameraSurfaceView mCameraSurfaceView;
    //private RectFocusView mRectOnCamera;
    private ImageButton takePicBtn;
    private TextView textView_local;
    private BaseLineView baseLineView;
    private DrawFreeView drawFreeView;

    private TextView textView_cancel;
    private TextView textView_redraw;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
        textView_cancel= (TextView) findViewById(R.id.cancel_draw);
        textView_redraw= (TextView) findViewById(R.id.re_draw);
        mCameraSurfaceView = (CameraSurfaceView) findViewById(R.id.cameraSurfaceView);
        drawFreeView = (DrawFreeView) findViewById(R.id.drawFreeView);
        baseLineView= (BaseLineView) findViewById(R.id.baseLine);
        //mRectOnCamera = (RectFocusView) findViewById(R.id.rectOnCamera);
        takePicBtn = (ImageButton) findViewById(R.id.takePic);
        textView_local = (TextView) findViewById(R.id.local_gallery);
        findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        drawFreeView.setCanUploadListener(new DrawFreeView.CanUploadListener() {
            @Override
            public void canUpload() {
                takePicBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.take_pic_back));
            }

            @Override
            public void cannotUpload() {
                takePicBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.upload_back));
            }
        });
        textView_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCameraSurfaceView.isPreviewing()){
                    drawFreeView.cancel();
                }
            }
        });

        textView_redraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCameraSurfaceView.isPreviewing()){
                    drawFreeView.redo();
                }
            }
        });
        textView_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraSurfaceView.isPreviewing()) {
                    //TODO:去相册中选取照片
                }else {
                    textView_local.setText("本地图库");
                    Drawable drawable = getResources().getDrawable(android.R.drawable.ic_menu_gallery);
                    //设置之前，必须调用setBounds()
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    textView_local.setCompoundDrawables(null, drawable, null, null);
                    takePicBtn.setImageResource(android.R.drawable.ic_menu_camera);
                    takePicBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.take_pic_back));
                    mCameraSurfaceView.reTake();
                    baseLineView.setVisibility(View.VISIBLE);//显示参考线
                    drawFreeView.setVisibility(View.GONE);//去掉蒙版
                }
            }
        });

        /*mRectOnCamera.setIAutoFocus(new RectFocusView.IAutoFocus() {
            @Override
            public void autoFocus() {
                mCameraSurfaceView.setAutoFocus();
            }
        });*/
        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraSurfaceView.isPreviewing()) {
                    mCameraSurfaceView.takePicture();
                    takePicBtn.setImageResource(android.R.drawable.ic_menu_upload);
                    takePicBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.upload_back));
                    textView_local.setText("重拍");
                    Drawable drawable = getResources().getDrawable(android.R.drawable.ic_menu_camera);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    textView_local.setCompoundDrawables(null, drawable, null, null);
                    baseLineView.setVisibility(View.INVISIBLE);//隐藏参考线
                    drawFreeView.setVisibility(View.VISIBLE);//显示蒙版
                } else {
                    if (drawFreeView.isCanUpload()){
                        //TODO:上传操作
                        final ProgressDialog dialog=new ProgressDialog(CameraActivity.this);
                        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.setMessage("正在上传，请稍后！");
                        dialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Intent intent=new Intent(CameraActivity.this,ShowAttributeActivity.class);
                                intent.putExtra("midNo","0623A3");
                                startActivity(intent);
                            }
                        },3000);
                    }else {
                        Toast.makeText(getApplicationContext(),"您还没有涂抹需要识别的文字！",Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }
}
