#include <jni.h>
#include <string>
#include <GuideFilter.h>
#include <iostream>
#include <android/bitmap.h>
#include <android/log.h>
#define TAG "jni_string"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define MAKE_RGBA(r, g, b, a) (((a) << 24) | ((r) << 16) | ((g) << 8) | (b))//???
#define RGBA_A(p) (((p) & 0xFF000000) >> 24)

extern "C" JNIEXPORT void JNICALL
Java_com_example_opencvdemo2_DrawView_GuideFilter(
        JNIEnv *env, jclass type, jobject bitmapI, jobject bitmapP){
    if (bitmapI == NULL || bitmapP == NULL){
        return;
    }
    AndroidBitmapInfo infoI, infoP;
    memset(&infoI, 0, sizeof(infoI));   //初始化置零
    memset(&infoP, 0, sizeof(infoP));   //初始化置零
    AndroidBitmap_getInfo(env, bitmapI, &infoI);    //获取图片信息
    AndroidBitmap_getInfo(env, bitmapP, &infoP);
    void *pixelsI = NULL;
    AndroidBitmap_lockPixels(env, bitmapI, &pixelsI);   //获取像素
    Mat I(infoI.height, infoI.width, CV_8UC4, pixelsI); //创建Mat对象，并将像素进行赋值
    Mat gray;
    cvtColor(I, gray, CV_BGRA2GRAY);    //注意Bitmap Config。8888对于BGRA通道
    cvtColor(I, I, CV_BGRA2RGB);        //转为RGB通道
    gray.convertTo(gray, CV_64FC1, 1.0 / 255);

    void *pixelsP = NULL;
    AndroidBitmap_lockPixels(env, bitmapP, &pixelsP);
    Mat P(infoP.height, infoP.width, CV_8UC4, pixelsP);
    cvtColor(P, P, CV_BGRA2GRAY);
    P.convertTo(P, CV_64FC1, 1.0 / 255);

    int r1 = I.cols / 100;          //引导滤波参数设置
    if (I.cols > I.rows){
        r1 = I.rows / 100;
    }
    Mat output = guidefilter(gray, P, r1, 0.001);

    output.convertTo(output, CV_8UC1);
    Mat dst[3] = {output, output, output};
    Mat dsts;
    merge(dst, 3, dsts);
    Mat result = dsts.mul(I);

    int a = 0, r = 0, g = 0, b = 0;

    for (int y = 0; y < infoI.height; ++y) {
        for (int x = 0; x < infoI.width; ++x) {
            int *pixel = NULL;
            pixel = ((int *) pixelsI) + y * infoI.width + x;
            r = result.at<Vec3b>(y, x)[0];
            g = result.at<Vec3b>(y, x)[1];
            b = result.at<Vec3b>(y, x)[2];
            a = RGBA_A(*pixel);
            *pixel = MAKE_RGBA(r, g, b, a);
        }
    }
    AndroidBitmap_unlockPixels(env, bitmapI);   //释放
    AndroidBitmap_unlockPixels(env, bitmapP);
}