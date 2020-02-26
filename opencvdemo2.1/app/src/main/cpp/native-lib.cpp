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
    AndroidBitmap_getInfo(env, bitmapI, &infoI);
    AndroidBitmap_getInfo(env, bitmapP, &infoP);
    void *pixelsI = NULL;
    AndroidBitmap_lockPixels(env, bitmapI, &pixelsI);
    Mat I(infoI.height, infoI.width, CV_8UC4, pixelsI);
    Mat gray;
    cvtColor(I, gray, CV_BGRA2GRAY);
    cvtColor(I, I, CV_BGRA2RGB);
    gray.convertTo(gray, CV_64FC1, 1.0 / 255);

    void *pixelsP = NULL;
    AndroidBitmap_lockPixels(env, bitmapP, &pixelsP);
    Mat P(infoP.height, infoP.width, CV_8UC4, pixelsP);
    cvtColor(P, P, CV_BGRA2GRAY);
    P.convertTo(P, CV_64FC1, 1.0 / 255);

    int r1 = I.cols / 10;
    if (I.cols > I.rows){
        r1 = I.rows / 10;
    }
    Mat output = guidefilter(gray, P, r1, 0.000001);

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
    AndroidBitmap_unlockPixels(env, bitmapI);
    AndroidBitmap_unlockPixels(env, bitmapP);
}