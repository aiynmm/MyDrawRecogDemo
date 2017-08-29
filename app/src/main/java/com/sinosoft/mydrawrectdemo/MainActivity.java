package com.sinosoft.mydrawrectdemo;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.sinosoft.mydrawrectdemo.imagerecon.ImgDealUtil;
import com.sinosoft.mydrawrectdemo.mycamera.CameraActivity;
import com.sinosoft.mydrawrectdemo.network.ImgToWordEntity;
import com.sinosoft.mydrawrectdemo.network.RecongnizeImgService;
import com.theartofdev.edmodo.cropper.CropImage;
import com.yalantis.ucrop.UCrop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 0x23;
    private TakePhotoIntentManager takePhotoIntentManager;
    private int type;

    public static final int CROP_CODE = 0x24;
    private ImageView imageView;
    private TextView content;

    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    private static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    private static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    private static final String DEFAULT_LANGUAGE = "chi_sim";
    /**
     * assets中的文件名
     */
    private static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    private static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

    private ProgressDialog dialog;
    private static final int COPY_FLAG = 1;
    private static final int RECON_FLAG = 2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RECON_FLAG) {
                dialog.dismiss();
                String reconText = (String) msg.obj;
                content.setText(reconText);
            } else if (msg.what == COPY_FLAG) {
                dialog.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        takePhotoIntentManager = new TakePhotoIntentManager(MainActivity.this);
        imageView = (ImageView) findViewById(R.id.img);
        content = (TextView) findViewById(R.id.content);
        showLoadDialog("正在复制识别文件到您的手机中，请稍等...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                initTessData();
                handler.sendEmptyMessage(COPY_FLAG);
            }
        }).start();
        findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
                type = 0;
            }
        });

        findViewById(R.id.crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
                type = 1;
            }
        });

        findViewById(R.id.ucrop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
                type = 2;
            }
        });

        findViewById(R.id.image_crop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
                type = 3;
            }
        });

        findViewById(R.id.like_baidu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.recognise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadDialog("正在识别图片，请稍等...");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TessBaseAPI tessBaseAPI = new TessBaseAPI();
                        tessBaseAPI.init(DATAPATH, DEFAULT_LANGUAGE);
                        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO);
                        tessBaseAPI.setImage(bitmap);
                        String text = tessBaseAPI.getUTF8Text();
                        Message message = handler.obtainMessage();
                        message.what = RECON_FLAG;
                        message.obj = text;
                        handler.sendMessage(message);
                        tessBaseAPI.clear();
                        tessBaseAPI.end();
                    }
                }).start();

            }
        });

        findViewById(R.id.cloud_recognise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadDialog("正在识别图片，请稍等...");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                byte[] bytes = outputStream.toByteArray();
                String base64Img = Base64.encodeToString(bytes, Base64.NO_WRAP);
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .build();
                RecongnizeImgService recongnizeImgService = retrofit.create(RecongnizeImgService.class);
                Call<ImgToWordEntity> call = recongnizeImgService.getRecognizeWord(base64Img);
                call.enqueue(new Callback<ImgToWordEntity>() {
                    @Override
                    public void onResponse(Call<ImgToWordEntity> call, Response<ImgToWordEntity> response) {
                        dialog.dismiss();
                        ImgToWordEntity entity = response.body();
                        if ("0".equals(entity.getError_code())) {
                            String codes = entity.getCodes();
                            String probability = entity.getProbability();
                            content.setText(codes + "\n" + probability);
                        }
                    }

                    @Override
                    public void onFailure(Call<ImgToWordEntity> call, Throwable t) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private static final String BASE_URL = "http://124.16.139.218:8014/";

    private void showLoadDialog(String message) {
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage(message);
        dialog.show();
    }


    /**
     * 初始化识别数据，将assets里的识别包拷到SD卡中
     */
    private void initTessData() {
        //如果存在就删掉
        File f = new File(LANGUAGE_PATH);
        if (f.exists()) {
            f.delete();
        }
        if (!f.exists()) {
            File p = new File(f.getParent());
            if (!p.exists()) {
                p.mkdirs();
            }
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        InputStream is = null;
        OutputStream os = null;
        try {
            is = this.getAssets().open(DEFAULT_LANGUAGE_NAME);
            File file = new File(LANGUAGE_PATH);
            os = new FileOutputStream(file);
            byte[] bytes = new byte[2048];
            int len = 0;
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
                if (os != null)
                    os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkCameraPermission() {
        int hasWriteContactsPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {//是否允许请求权限
            //是否设置了不再询问
            if (!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {
                showMessageOKCancel(MainActivity.this, "无法获取相机权限，请在设置中开启相机权限！",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                return;
            }
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        //具体要做的事！！！！！
        Intent intent = takePhotoIntentManager.dispatchTakePictureIntent();
        startActivityForResult(intent, TakePhotoIntentManager.TAKE_PHOTO);
    }

    private void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setTitle("权限申请")
                .setMessage(message)
                .setPositiveButton("我知道了", okListener)
                //.setNegativeButton("取消", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = takePhotoIntentManager.dispatchTakePictureIntent();
                startActivityForResult(intent, TakePhotoIntentManager.TAKE_PHOTO);
            } else {
                showMessageOKCancel(MainActivity.this, "无法获取相机权限，请在设置中开启相机权限！",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
            }
        }
    }

    private Uri cropUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "crop.jpg"));

    /**
     * 通过Uri传递图像信息以供裁剪
     *
     * @param uri
     */
    private void startImageZoom(Uri uri) {
        //构建隐式Intent来启动裁剪程序
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //输出图片的宽高均为150
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
        startActivityForResult(intent, CROP_CODE);
    }

    private Bitmap bitmap;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TakePhotoIntentManager.TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                if (takePhotoIntentManager == null) {
                    takePhotoIntentManager = new TakePhotoIntentManager(this);
                }
                takePhotoIntentManager.galleryAddPic();
                String path = takePhotoIntentManager.getCurrentPhotoPath();
                Intent intent;
                if (type == 0) {
                    intent = new Intent(MainActivity.this, DrawActivity.class);
                    intent.putExtra("path", path);
                    startActivity(intent);
                } else if (type == 1) {
                    //从相机拍照得到的是Bitmap类型，所以我们需要先将其转化为文件Uri以供裁剪。
                    //从图库选择的图片返回的是content类型的Uri，我们需要转化为文件类型的Uri才能进行裁剪。
                    startImageZoom(Uri.fromFile(new File(path)));
                } else if (type == 2) {
                    UCrop.Options options = new UCrop.Options();
                    options.setFreeStyleCropEnabled(true);
                    UCrop.of(Uri.fromFile(new File(path)), cropUri)
                            //.withAspectRatio(16, 9)//如果不指定，会有5个选项供选择
                            .withOptions(options)
                            .withMaxResultSize(100, 100)//如果不设置，会有一个默认值
                            .start(MainActivity.this);
                } else if (type == 3) {
                    CropImage.activity(Uri.fromFile(new File(path)))
                            .start(this);
                }

            }
        } else if (requestCode == CROP_CODE) {
            if (resultCode == RESULT_OK) {
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(cropUri));
                    imageView.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            try {
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            imageView.setImageBitmap(bitmap);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(resultUri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                //bitmap = ImgDealUtil.gray2Binary(ImgDealUtil.bitmap2Gray(bitmap));
                //预处理步骤：1.灰度化 2.二值化 3.锐化
                bitmap = ImgDealUtil.sharpenImageAmeliorate(ImgDealUtil.gray2Binary(ImgDealUtil.getGrayImg(bitmap)));
                imageView.setImageBitmap(bitmap);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
