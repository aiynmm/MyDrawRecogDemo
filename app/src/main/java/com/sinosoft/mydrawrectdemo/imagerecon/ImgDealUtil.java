package com.sinosoft.mydrawrectdemo.imagerecon;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Mars on 2017/6/16.
 */

public class ImgDealUtil {

    //图像灰度化
    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray = null;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }


    //对图像进行线性灰度变化
    public static Bitmap getGrayImg(Bitmap image) {
        //得到图像的宽度和长度
        int width = image.getWidth();
        int height = image.getHeight();
        //创建线性拉升灰度图像
        Bitmap linegray = null;
        linegray = image.copy(Bitmap.Config.ARGB_8888, true);
        //依次循环对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到每点的像素值
                int col = image.getPixel(i, j);
                int alpha = col & 0xFF000000;
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 增加了图像的亮度
                red = (int) (1.1 * red + 30);
                green = (int) (1.1 * green + 30);
                blue = (int) (1.1 * blue + 30);
                //对图像像素越界进行处理
                if (red >= 255) {
                    red = 255;
                }

                if (green >= 255) {
                    green = 255;
                }

                if (blue >= 255) {
                    blue = 255;
                }
                // 新的ARGB
                int newColor = alpha | (red << 16) | (green << 8) | blue;
                //设置新图像的RGB值
                linegray.setPixel(i, j, newColor);
            }
        }
        return linegray;
    }

    // 该函数实现对图像进行二值化处理
    public static Bitmap gray2Binary(Bitmap graymap) {
        //得到图形的宽度和长度
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        //创建二值化图像
        Bitmap binarymap = null;
        binarymap = graymap.copy(Bitmap.Config.ARGB_8888, true);
        //int grayLimitValue=getOtsuHresholdValue(graymap);//大律法
        //int grayLimitValue = getIterationHresholdValue(graymap);//迭代法
        int grayLimitValue = iterationGetThreshold(bitmap2Pix(graymap));
        Log.e("ImgDealUtil", "算出的灰度阈值为" + grayLimitValue);
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到当前像素的值
                int col = binarymap.getPixel(i, j);
                //得到alpha通道的值
                int alpha = col & 0xFF000000;
                //得到图像的像素RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);

                //对图像进行二值化处理
                if (gray <= grayLimitValue) {//这里的数据可以根据算法得出
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 新的ARGB
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;
                //设置新图像的当前像素值
                binarymap.setPixel(i, j, newColor);
            }
        }
        return binarymap;
    }

    //图片二值化处理，二值化图像的阈值算法

    //大律法
    private static int getOtsuHresholdValue(Bitmap graymap) {
        //得到图形的宽度和长度
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        int[][] grays = new int[width][height];
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到当前像素的值
                int col = graymap.getPixel(i, j);
                //得到alpha通道的值
                int alpha = col & 0xFF000000;
                //得到图像的像素RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grays[i][j] = gray;
            }
        }
        for (int k = 0; k < grays.length; k++) { // 直接调用数组arrays里的sort()这个方法对里边的一维数组进行排序
            Arrays.sort(grays[k]);
        }
        int minOwnGrayValue = grays[0][0];
        int maxOwnGrayValue = grays[width - 1][height - 1];
        int T = 0;
        double U = 0, U0 = 0, U1 = 0;
        double G = 0;
        for (int i = minOwnGrayValue; i <= maxOwnGrayValue; i++) {
            double s = 0, l = 0, cs = 0, cl = 0;
            for (int j = 0; j < width - 1; j++) {
                for (int k = 0; k < height - 1; k++) {
                    int gray = graymap.getPixel(i, j);
                    if (gray < i) {
                        s += gray;
                        cs++;
                    }
                    if (gray > i) {
                        l += gray;
                        cl++;
                    }
                }
            }
            U0 = s / cs;
            U1 = l / cl;
            U = (s + l) / (cs + cl);
            double g = (cs / (cs + cl)) * (U0 - U) * (U0 - U)
                    + (cl / (cl + cs)) * (U1 - U) * (U1 - U);
            if (g > G) {
                T = i;
                G = g;
            }
        }
        return T;
    }

    //迭代法
    //首先需要获得中值：T = (maxGrayValue + minGrayValue) / 2;   公式（2-5）
    //maxGrayValue指的是最大灰度值；minGrayValue指的是最小灰度值。
    //将T视为阈值，大于T的为目标部分，小于T的为背景部分。再分别获取目标和背景的像素色值平均值T1和T2，
    // 获取新的阈值（T1+T2）/2；将新的阈值赋值给T重复获取新阈值，直到两个阈值一样而且连续的时候，
    // 将该阈值视为最终获取的阈值

    private static int getIterationHresholdValue(Bitmap graymap) {
        //得到图形的宽度和长度
        int width = graymap.getWidth();
        int height = graymap.getHeight();
        int[][] grays = new int[width][height];
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到当前像素的值
                int col = graymap.getPixel(i, j);
                //得到alpha通道的值
                int alpha = col & 0xFF000000;
                //得到图像的像素RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grays[i][j] = gray;
            }
        }
        for (int k = 0; k < grays.length; k++) { // 直接调用数组arrays里的sort()这个方法对里边的一维数组进行排序
            Arrays.sort(grays[k]);
        }
        int minGrayValue = grays[0][0];
        Log.e("ImgDealUtil", "算出的最小灰度值为" + minGrayValue);
        int maxGrayValue = grays[width - 1][height - 1];
        Log.e("ImgDealUtil", "算出的最大灰度值为" + maxGrayValue);
        int T1;
        int T2 = (maxGrayValue + minGrayValue) / 2;
        do {
            T1 = T2;
            double s = 0, l = 0, cs = 0, cl = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int gray = graymap.getPixel(i, j);
                    if (gray < T1) {
                        s += gray;
                        cs++;
                    }
                    if (gray > T1) {
                        l += gray;
                        cl++;
                    }
                }
            }
            T2 = (int) (s / cs + l / cl) / 2;
        } while (T1 != T2);
        return T1;
    }

    /**
     * 得到bitmap的一维灰度数组
     *
     * @param bitmap
     * @return
     */
    private static int[] bitmap2Pix(Bitmap bitmap) {
        //得到图形的宽度和长度
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组
        //依次循环，对图像的像素进行处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                //得到当前像素的值
                int col = bitmap.getPixel(i, j);
                //得到alpha通道的值
                int alpha = col & 0xFF000000;
                //得到图像的像素RGB的值
                int red = (col & 0x00FF0000) >> 16;
                int green = (col & 0x0000FF00) >> 8;
                int blue = (col & 0x000000FF);
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                pixels[i * height + j] = gray;
            }
        }
        return pixels;
    }

    /**
     * 用迭代法 求最佳阀值
     *
     * @param pix 灰度像素数组
     * @return 最佳阀值
     */
    public static int iterationGetThreshold(int[] pix) {
        int min = pix[0], max = pix[0];
        for (int i = 0; i < pix.length; i++) {
            if (pix[i] > 255) {
                pix[i] = 255;
            }
            if (pix[i] < 0) {
                pix[i] = 0;
            }
            if (min > pix[i])
                min = pix[i];
            if (max < pix[i])
                max = pix[i];
        }
        double histo[] = getHisto(pix);
        int threshold = 0;
        int newThreshold = (int) ((min + max) / 2);
        while (threshold != newThreshold) {
            double sum1 = 0, sum2 = 0, w1 = 0, w2 = 0;
            int avg1, avg2;
            for (int i = min; i < newThreshold; i++) {
                sum1 += histo[i] * i;
                w1 += histo[i];
            }
            avg1 = (int) (sum1 / w1);
            for (int i = newThreshold; i < max; i++) {
                sum2 += histo[i] * i;
                w2 += histo[i];
            }
            avg2 = (int) (sum2 / w2);
            //System.out.println("avg1:" + avg1 + "  avg2:" + avg2 + "  newThreshold:" + newThreshold);
            threshold = newThreshold;
            newThreshold = (avg1 + avg2) / 2;
        }
        return newThreshold;
    }

    /**
     * 求图像的灰度直方图
     *
     * @param pix 一维的灰度图像像素值
     * @return 0-255的 像素值所占的比率
     */
    public static double[] getHisto(int pix[]) {
        double histo[] = new double[256];
        for (int i = 0; i < pix.length; i++) {
            //System.out.println("pix[i]:" + pix[i]);
            if (pix[i] > 255) {
                pix[i] = 255;
            }
            if (pix[i] < 0) {
                pix[i] = 0;
            }
            histo[pix[i]]++;
        }
        for (int i = 0; i < 255; i++) {
            histo[i] = (double) histo[i] / pix.length;
        }
        return histo;
    }

    /**
     * 求二值图像
     *
     * @param pix       像素矩阵数组
     * @param threshold 阀值
     * @return 处理后的数组
     */
    public int[] threshold(int pix[], int threshold) {
        for (int i = 0; i < pix.length; i++) {
            if (pix[i] <= threshold) {
                pix[i] = 0;
            } else {
                pix[i] = 255;
            }
        }
        return pix;
    }


    /**
     * 得到图片一个像素点的灰度值
     * @param pixel
     * @return
     */
    private static int getGray(int pixel) {
        int red = (pixel & 0x00FF0000) >> 16;
        int green = (pixel & 0x0000FF00) >> 8;
        int blue = (pixel & 0x000000FF);
        // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB
        int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
        return gray;
    }

    //图像去除噪点本文使用的算法是取区域像素中值的方法进行去噪
    public static int getCenterValue(Bitmap img, int x, int y) {
        int[] pix = new int[9]; //该点以及周围8个点共9个点
        int h = img.getHeight() - 1; //获取高度像素值
        int w = img.getWidth() - 1; //获取宽度像素值
        if (x > 0 && y > 0) //如果点不再上边框和左边框，则存在pix[0]
            pix[0] = getGray(img.getPixel(x - 1, y - 1));
        if (y > 0)  //如果点不是上边框，则存在pix[1]
            pix[1] = getGray(img.getPixel(x, y - 1));
        if (x < h && y > 0) //如果点不是右边框和上边框，则存在pix[2]
            pix[2] = getGray(img.getPixel(x + 1, y - 1));
        if (x > 0) //如果点不是左边框，则存在pix[3]
            pix[3] = getGray(img.getPixel(x - 1, y));
        pix[4] = getGray(img.getPixel(x, y)); //pix[4]为要获取中值的点
        if (x < h) //如果没在右边框，则存在pix[5]
            pix[5] = getGray(img.getPixel(x + 1, y));
        if (x > 0 && y < w) //如果没在上边框和有边框，则存在pix[6]
            pix[6] = getGray(img.getPixel(x - 1, y + 1));
        if (y < w) //如果没在右边框，则存在pix[7]
            pix[7] = getGray(img.getPixel(x, y + 1));
        if (x < h && y < w) //如果没在右边框和下边框，则存在pix[8]
            pix[8] = getGray(img.getPixel(x + 1, y + 1));
        int max = 0, min = 255;
        for (int i = 0; i < pix.length; i++) {
            if (pix[i] > max)
                max = pix[i];
            if (pix[i] < min)
                min = pix[i];
        }
        int count = 0;
        int i = 0;
        for (i = 0; i < 9; i++) {
            if (pix[i] >= min)
                count++;
            if (count == 5)
                break;
        }
        return pix[i];
    }

    /**
     * 图片平滑处理
     * 3*3掩模处理（平均处理），降低噪声
     *
     * @param mBitmapSrc 图片源
     * @return Bitmap
     */

    public static Bitmap smoothImage(Bitmap mBitmapSrc) {
        int w = mBitmapSrc.getWidth();
        int h = mBitmapSrc.getHeight();
        int[] data = new int[w * h];
        mBitmapSrc.getPixels(data, 0, w, 0, 0, w, h);
        int[] resultData = new int[w * h];
        try {
            resultData = filter(data, w, h);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap newBitmap = Bitmap.createBitmap(resultData, w, h, Bitmap.Config.ARGB_8888);
        return newBitmap;
    }

    private static int[] filter(int[] data, int width, int height) throws Exception {
        int filterData[] = new int[data.length];
        int min = 10000;
        int max = -10000;
        if (data.length != width * height) return filterData;
        try {
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (i == 0 || i == 1 || i == height - 1 || i == height - 2 || j == 0 || j == 1 || j == width - 1 || j == width - 2) {
                        filterData[i * width + j] = data[i * width + j];
                    } else {
                        double average;             //中心的九个像素点
                        average = (data[i * width + j] + data[i * width + j - 1] + data[i * width + j + 1]
                                + data[(i - 1) * width + j] + data[(i - 1) * width + j - 1] + data[(i - 1) * width + j + 1]
                                + data[(i + 1) * width + j] + data[(i + 1) * width + j - 1] + data[(i + 1) * width + j + 1]) / 9;
                        filterData[i * width + j] = (int) (average);
                    }
                    if (filterData[i * width + j] < min)
                        min = filterData[i * width + j];
                    if (filterData[i * width + j] > max)
                        max = filterData[i * width + j];
                }
            }
            for (int i = 0; i < width * height; i++) {
                filterData[i] = (filterData[i] - min) * 255 / (max - min);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(e);
        }
        return filterData;
    }

    /**
     * 图片锐化（拉普拉斯变换）
     *
     * @param mBitmapSrc 图片源
     * @return Bitmap
     */
    public static Bitmap sharpenImageAmeliorate(Bitmap mBitmapSrc) {
        // 拉普拉斯矩阵
        int[] laplacian = new int[]{-1, -1, -1, -1, 9, -1, -1, -1, -1};
        int width = mBitmapSrc.getWidth();
        int height = mBitmapSrc.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int pixColor = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int idx = 0;
        float alpha = 0.3F;
        int[] pixels = new int[width * height];
        mBitmapSrc.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                idx = 0;
                for (int m = -1; m <= 1; m++) {
                    for (int n = -1; n <= 1; n++) {
                        pixColor = pixels[(i + n) * width + k + m];
                        pixR = Color.red(pixColor);
                        pixG = Color.green(pixColor);
                        pixB = Color.blue(pixColor);
                        newR = newR + (int) (pixR * laplacian[idx] * alpha);
                        newG = newG + (int) (pixG * laplacian[idx] * alpha);
                        newB = newB + (int) (pixB * laplacian[idx] * alpha);
                        idx++;
                    }
                }
                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));
                pixels[i * width + k] = Color.argb(255, newR, newG, newB);
                newR = 0;
                newG = 0;
                newB = 0;
            }
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}


