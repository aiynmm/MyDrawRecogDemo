package com.sinosoft.mydrawrectdemo.mycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.sinosoft.mydrawrectdemo.R;

import java.util.ArrayList;

/**
 * Created by Mars on 2017/6/7.
 */

public class DrawFreeView extends View {
    private Paint mPaint;

    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;// 画布的画笔
    private int width, height;

    private Path mPath;
    private float oldX, oldY;
    private ArrayList<Path> paths;

    private int mScreenWidth;
    private int mScreenHeight;

    public DrawFreeView(Context context) {
        this(context, null);
    }

    public DrawFreeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setPaintStyle();
        getScreenMetrix(context);
        initCanvas();
    }

    //初始化画笔样式
    private void setPaintStyle() {
        paths=new ArrayList<>();

        mPaint = new Paint();
        mPaint.setStrokeWidth(100);
        mPaint.setColor(Color.TRANSPARENT);//此处设置无意义，可以不设置
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
        mPaint.setPathEffect(new CornerPathEffect(360));
        PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        mPaint.setXfermode(mode);
    }

    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }

    //初始化画笔画板
    public void initCanvas() {
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        //不能在ondraw()上的canvas直接作画，需要单独一套的bitmap以及canvas记录路径
        mBitmap = Bitmap.createBitmap(mScreenWidth, mScreenHeight, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        mCanvas.drawColor(getResources().getColor(R.color.mask_color));//设置透明是为了之后的添加背景
    }

    public void cancel() {
        if (paths != null && paths.size() > 0) {
            paths.remove(paths.size() - 1);
            redrawOnBitmap();
            if (paths.size()>0){
                canUploadListener.canUpload();
            }else {
                canUploadListener.cannotUpload();
            }
        }
    }

    public void redo() {
        if (paths != null && paths.size() > 0) {
            paths.clear();
            redrawOnBitmap();
            canUploadListener.cannotUpload();
        }
    }

    public boolean isCanUpload(){
        return paths.size()>0;
    }

   public interface CanUploadListener{
       void canUpload();
       void cannotUpload();
   }
   private CanUploadListener canUploadListener;

    public CanUploadListener getCanUploadListener() {
        return canUploadListener;
    }

    public void setCanUploadListener(CanUploadListener canUploadListener) {
        this.canUploadListener = canUploadListener;
    }

    //将剩下的path重绘
    private void redrawOnBitmap() {
        initCanvas();
        for (Path drawPath : paths) {
            mCanvas.drawPath(drawPath, mPaint);
        }
        invalidate();// 刷新
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 将前面已经画过得显示出来
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if (mPath != null) {
            // 实时的显示
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                float x1 = event.getX();
                float y1 = event.getY();
                mPath.moveTo(x1, y1);
                oldX = x1;
                oldY = y1;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float x2 = event.getX();
                float y2 = event.getY();

                float dx = Math.abs(x2 - oldX);
                float dy = Math.abs(y2 - oldY);
                if (dx >= 3 || dy >= 3) {
                    mPath.lineTo(x2, y2);
                    oldX = x2;
                    oldY = y2;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);
                paths.add(mPath);
                mPath = null;// 重新置空
                invalidate();
                canUploadListener.canUpload();
                break;
        }
        return true;
    }
}
