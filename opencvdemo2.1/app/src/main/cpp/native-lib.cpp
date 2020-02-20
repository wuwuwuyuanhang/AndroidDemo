#include <jni.h>
#include <string>
#include <GuideFilter.h>
#include <iostream>
#include <android/bitmap.h>

extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_opencvdemo2_DrawView_GuideFilter(
JNIEnv *env, jclass type, jintArray I_, jintArray P_, jint r, jdouble eps, jint h, jint w ){
    jint *imageI = env->GetIntArrayElements(I_, NULL);
    Mat I(h, w, CV_8UC1, (unsigned char *) imageI);
    jint *imageP = env->GetIntArrayElements(P_, NULL);
    Mat P(h, w, CV_8UC1, (unsigned char *) imageP);
    Mat output = guidefilter(I, P, r, eps);
    int size = output.cols * output.rows;
    jintArray  result = env->NewIntArray(size);
    int outInt[size];
    for (int i = 0; i < output.rows; i++){
        for (int j = 0; j < output.cols; j++) {
            outInt[i * w + j] = output.at<uchar>(i, j);
        }
    }
    env->SetIntArrayRegion(result, 0, size, outInt);
    env->ReleaseIntArrayElements(I_, imageI, 0);
    env->ReleaseIntArrayElements(P_, imageP, 0);
    return result;
}