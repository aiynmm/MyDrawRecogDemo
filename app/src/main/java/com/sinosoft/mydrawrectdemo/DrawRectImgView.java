package com.sinosoft.mydrawrectdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Mars on 2017/5/16.
 */

public class DrawRectImgView extends AppCompatImageView {
    private Paint mPaint;
    private Path mPath;
    private float oldX, oldY;
    private int currentStyle;//默认为0
    public static final int DRAW_FREE = 0;
    public static final int DRAW_RECT = 1;
    private ArrayList<DrawPath> paths;
    private DrawPath drawPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;// 画布的画笔
    private int width, height;


    public DrawRectImgView(Context context) {
        this(context, null);
    }

    public DrawRectImgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCurrentStyle(DRAW_FREE);
        paths = new ArrayList<>();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setCurrentStyle(int currentStyle) {
        this.currentStyle = currentStyle;
        setPaintStyle();
    }

    public int getCurrentStyle() {
        return currentStyle;
    }

    private class DrawPath {
        Path path;
        Paint paint;
    }

    //初始化画笔样式
    private void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(20);
        mPaint.setColor(Color.GREEN);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        if (currentStyle == DRAW_FREE) {
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
            mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
            mPaint.setPathEffect(new CornerPathEffect(360));
        } else if (currentStyle == DRAW_RECT) {
            mPaint.setStyle(Paint.Style.STROKE);
        }
        /*if (currentStyle == 1) {//普通画笔功能

        } else {//橡皮擦
            mPaint.setAlpha(0);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));//这两个方法一起使用才能出现橡皮擦效果
            mPaint.setColor(Color.TRANSPARENT);
            mPaint.setStrokeWidth(50);
            //currentDrawGraphics = DRAW_PATH;//使用橡皮擦时默认用线的方式擦除
        }*/
    }

    public void cancel() {
        if (paths != null && paths.size() > 0) {
            paths.remove(paths.size() - 1);
            redrawOnBitmap();
        }
    }

    public void redo() {
        if (paths != null && paths.size() > 0) {
            paths.clear();
            redrawOnBitmap();
        }
    }

    //初始化画笔画板
    public void initCanvas() {

        setPaintStyle();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        //不能在ondraw()上的canvas直接作画，需要单独一套的bitmap以及canvas记录路径
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //mBitmap.eraseColor(Color.argb(0, 0, 0, 0));//橡皮擦的设置
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
        //mCanvas.drawColor(Color.TRANSPARENT);//设置透明是为了之后的添加背景
    }

    //将剩下的path重绘
    private void redrawOnBitmap() {
        initCanvas();
        for (DrawPath drawPath : paths) {
            mCanvas.drawPath(drawPath.path, drawPath.paint);
        }
        invalidate();// 刷新
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        initCanvas();
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
                drawPath = new DrawPath();
                drawPath.path = mPath;
                drawPath.paint = mPaint;

                float x1 = event.getX();
                float y1 = event.getY();
                if (currentStyle == DRAW_FREE) {
                    mPath.moveTo(x1, y1);
                } else if (currentStyle == DRAW_RECT) {

                }
                oldX = x1;
                oldY = y1;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                float x2 = event.getX();
                float y2 = event.getY();
                if (currentStyle == DRAW_FREE) {
                    float dx = Math.abs(x2 - oldX);
                    float dy = Math.abs(y2 - oldY);
                    if (dx >= 3 || dy >= 3) {
                        mPath.lineTo(x2, y2);
                        oldX = x2;
                        oldY = y2;
                    }
                    invalidate();

                } else if (currentStyle == DRAW_RECT) {
                    mPath.reset();
                    if (oldX <= x2 && oldY <= y2) {
                        RectF rectF = new RectF(oldX, oldY, x2, y2);
                        mPath.addRect(rectF, Path.Direction.CCW);
                    } else if (oldX <= x2 && oldY >= y2) {
                        RectF rectF = new RectF(oldX, y2, x2, oldY);
                        mPath.addRect(rectF, Path.Direction.CCW);
                    } else if (oldX >= x2 && oldY >= y2) {
                        RectF rectF = new RectF(x2, y2, oldX, oldY);
                        mPath.addRect(rectF, Path.Direction.CCW);
                    } else if (oldX >= x2 && oldY <= y2) {
                        RectF rectF = new RectF(x2, oldY, oldX, y2);
                        mPath.addRect(rectF, Path.Direction.CCW);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                //mPath.lineTo(oldX, oldY);
                mCanvas.drawPath(mPath, mPaint);
                paths.add(drawPath);
                mPath = null;// 重新置空
                invalidate();
                break;
        }
        return true;
    }
}
