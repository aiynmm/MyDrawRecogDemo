package com.sinosoft.mydrawrectdemo.mycamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Mars on 2017/6/7.
 */

public class BaseLineView extends SurfaceView {
    private SurfaceHolder holder;
    public BaseLineView(Context context) {
        this(context,null);
    }

    public BaseLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView(){
        holder = getHolder();//获得surfaceHolder引用
        holder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);//一定要加这一条属性！！！！！
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Canvas canvas = holder.lockCanvas();
                canvas.drawColor(Color.TRANSPARENT);
                Paint p = new Paint();
                p.setAntiAlias(true);
                p.setColor(Color.WHITE);
                p.setStyle(Paint.Style.FILL);
                int width=getWidth();
                int height=getHeight();
                //3条横线
                canvas.drawLine(0,height/4, width, height/4, p);
                canvas.drawLine(0,height/2, width, height/2, p);
                canvas.drawLine(0,height*3/4, width, height*3/4, p);

                //文本
               /* Paint pText = new Paint();
                pText.setAntiAlias(true);
                pText.setColor(Color.WHITE);
                pText.setStyle(Paint.Style.FILL);
                pText.setTextAlign(Paint.Align.CENTER);*/
                p.setTextSize(40);
                String text="文本平行参考线";
                canvas.drawText(text,width/2-p.measureText(text)/2,height/4,p);

                //2条竖线
                canvas.drawLine(width/3,0, width/3, height, p);
                canvas.drawLine(width*2/3,0, width*2/3, height, p);
                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

}
