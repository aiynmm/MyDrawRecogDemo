package com.sinosoft.mydrawrectdemo.mycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sinosoft.mydrawrectdemo.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Mars on 2017/6/5.
 * 在API21添加了Camera2，并废弃了Camera，本次先了解一下Camera，之后再说Camera2
 */

public class CameraSurfaceView extends SurfaceView {
    private static final String TAG = "CameraSurfaceView";

    private Context mContext;
    private SurfaceHolder holder;
    private Camera mCamera;

    private boolean isPreviewing;

    private int mScreenWidth;
    private int mScreenHeight;

    private Paint mPaint;
    private Path mPath;
    private float oldX, oldY;
    private ArrayList<Path> paths;

    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Paint mBitmapPaint;// 画布的画笔

    private boolean mIsDrawing;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        //getScreenMetrix(context);
        setPaintStyle();
        initView();

    }

    //初始化画笔样式
    private void setPaintStyle() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(40);
        mPaint.setColor(Color.GREEN);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);// 设置外边缘
        mPaint.setStrokeCap(Paint.Cap.ROUND);// 形状
        mPaint.setPathEffect(new CornerPathEffect(360));
       /* PorterDuffXfermode mode=new PorterDuffXfermode(PorterDuff.Mode.XOR);
        mPaint.setXfermode(mode);*/
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
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        paths = new ArrayList<>();
        //不能在ondraw()上的canvas直接作画，需要单独一套的bitmap以及canvas记录路径
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);  //所有mCanvas画的东西都被保存在了mBitmap中
    }

    //将剩下的path重绘
    private void redrawOnBitmap() {
        initCanvas();
        for (Path drawPath : paths) {
            mCanvas.drawPath(drawPath,mPaint);
        }
        invalidate();// 刷新
    }

   /* private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }*/


    /**
     * 用来绘制图形的方法
     */
    public void draw() {
        Canvas canvas = holder.lockCanvas(); // 锁定canvas
        if (!isPreviewing) {
            canvas.drawColor(getResources().getColor(R.color.upload_back_color));
            //canvas.drawColor(Color.GREEN);
            // 将前面已经画过得显示出来
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            if (mPath != null) {
                // 实时的显示
                canvas.drawPath(mPath, mPaint);
            }
        }
        holder.unlockCanvasAndPost(canvas); // 解锁canvas
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (!isPreviewing) {
                    mPath = new Path();

                    float x1 = event.getX();
                    float y1 = event.getY();

                    mPath.moveTo(x1, y1);

                    oldX = x1;
                    oldY = y1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isPreviewing) {
                    float x2 = event.getX();
                    float y2 = event.getY();

                    float dx = Math.abs(x2 - oldX);
                    float dy = Math.abs(y2 - oldY);
                    if (dx >= 3 || dy >= 3) {
                        mPath.lineTo(x2, y2);
                        oldX = x2;
                        oldY = y2;
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isPreviewing) {
                    mCanvas.drawPath(mPath, mPaint);
                    paths.add(mPath);
                    mPath = null;// 重新置空
                }
                break;
        }
        return true;
    }

    private void initView() {
        holder = getHolder();//获得surfaceHolder引用
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                initCanvas();//创建之后初始化一下画板
                //camera preview的同时是不可以进行Canvas的绘制的，lockCanvas()的值就是null。
                Log.i(TAG, "surfaceCreated");
                if (mCamera == null) {
                    mCamera = Camera.open();//开启相机
                    try {
                        setCameraParams();
                        mCamera.setPreviewDisplay(holder);//摄像头画面显示在Surface上
                        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
                        mCamera.startPreview();
                        isPreviewing = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "surfaceDestroyed");
                mCamera.cancelAutoFocus();
                mCamera.stopPreview();//停止预览
                mCamera.release();//释放相机资源
                mCamera = null;
                holder = null;
            }
        });
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置类型
    }

    public void setAutoFocus() {
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    Log.i(TAG, "onAutoFocus success=" + success);
                }
            }
        });
    }

    private void setCameraParams() {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);//设置自动对焦
        parameters.setRotation(90);
        mCamera.setParameters(parameters);

        // 短边比长边
        final float ratio = (float) getWidth() / getHeight();

        // 获取摄像头支持的PictureSize列表
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        /**从列表中选取合适的分辨率*/
        Camera.Size picSize = findBestPictureSize(pictureSizeList, parameters.getPictureSize(), ratio);
        parameters.setPictureSize(picSize.width, picSize.height);

        // 获取摄像头支持的PreviewSize列表
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();

        Camera.Size preSize = findBestPreviewSize(previewSizeList, parameters.getPreviewSize(), picSize, ratio);
        parameters.setPreviewSize(preSize.width, preSize.height);

        /*ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getWidth() * preSize.width / preSize.height;
        setLayoutParams(params);*/

        parameters.setJpegQuality(100); // 设置照片质量
        mCamera.setParameters(parameters);

    }

    /**
     * 找到短边比长边大于于所接受的最小比例的最大尺寸
     *
     * @param sizes       支持的尺寸列表
     * @param defaultSize 默认大小
     * @param minRatio    相机图片短边比长边所接受的最小比例
     * @return 返回计算之后的尺寸
     */
    private Camera.Size findBestPictureSize(List<Camera.Size> sizes, Camera.Size defaultSize, float minRatio) {
        final int MIN_PIXELS = 320 * 480;

        sortSizes(sizes);

        Iterator<Camera.Size> it = sizes.iterator();
        while (it.hasNext()) {
            Camera.Size size = it.next();
            //移除不满足比例的尺寸
            if ((float) size.height / size.width <= minRatio) {
                it.remove();
                continue;
            }
            //移除太小的尺寸
            if (size.width * size.height < MIN_PIXELS) {
                it.remove();
            }
        }

        // 返回符合条件中最大尺寸的一个
        if (!sizes.isEmpty()) {
            return sizes.get(0);
        }
        // 没得选，默认吧
        return defaultSize;
    }

    /**
     * @param sizes
     * @param defaultSize
     * @param pictureSize 图片的大小
     * @param minRatio    preview短边比长边所接受的最小比例
     * @return
     */
    private Camera.Size findBestPreviewSize(List<Camera.Size> sizes, Camera.Size defaultSize,
                                            Camera.Size pictureSize, float minRatio) {
        final int pictureWidth = pictureSize.width;
        final int pictureHeight = pictureSize.height;
        boolean isBestSize = (pictureHeight / (float) pictureWidth) > minRatio;
        sortSizes(sizes);

        Iterator<Camera.Size> it = sizes.iterator();
        while (it.hasNext()) {
            Camera.Size size = it.next();
            if ((float) size.height / size.width <= minRatio) {
                it.remove();
                continue;
            }

            // 找到同样的比例，直接返回
            if (isBestSize && size.width * pictureHeight == size.height * pictureWidth) {
                return size;
            }
        }

        // 未找到同样的比例的，返回尺寸最大的
        if (!sizes.isEmpty()) {
            return sizes.get(0);
        }

        // 没得选，默认吧
        return defaultSize;
    }

    private static void sortSizes(List<Camera.Size> sizes) {
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                return b.height * b.width - a.height * a.width;
            }
        });
    }

    public void takePicture() {
        // 当调用camera.takePiture方法后，camera会关闭了预览，如果需要继续拍照，需要调用startPreview()来重新开启预览
        if (isPreviewing) {
            mCamera.takePicture(null, null, jpeg);
            isPreviewing = false;
        }
    }

    //重拍
    public void reTake() {
        mCamera.startPreview();// 开启预览
        isPreviewing = true;
    }

    public boolean isPreviewing() {
        return isPreviewing;
    }

    public void setPreviewing(boolean previewing) {
        isPreviewing = previewing;
    }

    // 拍照瞬间调用
    private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            Log.i(TAG, "shutter");
        }
    };

    // 获得没有压缩过的图片数据
    private Camera.PictureCallback raw = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            Log.i(TAG, "raw");

        }
    };

    //创建jpeg图片回调数据对象
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera Camera) {
            BufferedOutputStream bos = null;
            Bitmap bm = null;
            try {
                // 获得图片
                bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Log.i(TAG, "Environment.getExternalStorageDirectory()=" + Environment.getExternalStorageDirectory());
                    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/yt" + System.currentTimeMillis() + ".jpg";//照片保存路径
                    File file = new File(filePath);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(file));
                    bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中

                } else {
                    Toast.makeText(mContext, "没有检测到内存卡", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    bos.flush();//输出
                    bos.close();//关闭
                    bm.recycle();// 回收bitmap空间
                    //mCamera.stopPreview();// 关闭预览
                    //mCamera.startPreview();// 开启预览
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    };

}
