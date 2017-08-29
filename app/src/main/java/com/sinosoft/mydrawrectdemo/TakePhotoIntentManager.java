package com.sinosoft.mydrawrectdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Admin on 2017/2/14.
 */

public class TakePhotoIntentManager {
    private String mCurrentPhotoPath;
    private Context mContext;

    public static final int TAKE_PHOTO = 0x23;

    public TakePhotoIntentManager(Context mContext) {
        this.mContext = mContext;
    }

    private File createImageFile() {
        String imgName = getCurrTime() + ".jpg";
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imgName);
        mCurrentPhotoPath = file.getAbsolutePath();
        return file;
    }

    private String getCurrTime() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault());
        return format.format(date);//按所要格式输出当前时间
    }

    public Intent dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            // Create the File where the photo should go
            File file = createImageFile();
            Uri photoFile;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                String authority = mContext.getApplicationInfo().packageName + ".provider";
                photoFile = FileProvider.getUriForFile(this.mContext.getApplicationContext(), authority, file);
            } else {
                photoFile = Uri.fromFile(file);
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
            }
        }
        return takePictureIntent;
    }

    public void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        if (TextUtils.isEmpty(mCurrentPhotoPath)) {
            return;
        }
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);
    }

    public String getCurrentPhotoPath() {
        return mCurrentPhotoPath;
    }
}
