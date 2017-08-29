package com.sinosoft.mydrawrectdemo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Mars on 2017/5/16.
 */

public class DrawActivity extends AppCompatActivity {
    private DrawRectImgView drawRectImgView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        getSupportActionBar().setTitle("");
        drawRectImgView = (DrawRectImgView) findViewById(R.id.draw_rect_view);

        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        //String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Pictures/Screenshots/S70522-190057.jpg";
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        drawRectImgView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.switch_type, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.switch_type:
                if (drawRectImgView.getCurrentStyle() == DrawRectImgView.DRAW_RECT) {
                    drawRectImgView.setCurrentStyle(DrawRectImgView.DRAW_FREE);
                } else {
                    drawRectImgView.setCurrentStyle(DrawRectImgView.DRAW_RECT);
                }
                return true;
            case R.id.save:
                drawRectImgView.setDrawingCacheEnabled(true);
                Bitmap bitmap = drawRectImgView.getDrawingCache(true);//将一个View转换为bitmap
                final File file = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        //压缩bitmap
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
                        Toast.makeText(getApplicationContext(),"保存成功！",Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            case R.id.cancel:
                drawRectImgView.cancel();
                return true;
            case R.id.redo:
                drawRectImgView.redo();
                return true;
            case R.id.upload:
                final ProgressDialog dialog=new ProgressDialog(this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage("正在上传，请稍后！");
                dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        Intent intent=new Intent(DrawActivity.this,ShowAttributeActivity.class);
                        intent.putExtra("midNo","0623A3");
                        startActivity(intent);
                        finish();
                    }
                },3000);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
