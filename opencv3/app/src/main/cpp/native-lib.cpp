#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#define DRAW_RECT 0
#define DRAW_LINE 1

#define MAKE_RGBA(r, g, b, a) (((a) << 24) | ((r) << 16) | ((g) << 8) | (b))

using namespace std;
using namespace cv;


Mat bgModel, fgModel;

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_dell_opencv3_MainActivity_splitImage(JNIEnv *env, jobject instance, jintArray buf1, jintArray buf2,
                                                      jint w, jint h, jint startX, jint startY, jint currentX, jint currentY, jint mode) {
    //读取int数组并转为Mat类型
    jint *cbuf, *cmask;
    cbuf = env->GetIntArrayElements(buf1,JNI_FALSE);
    cmask = env->GetIntArrayElements(buf2, JNI_FALSE);
    if (NULL == cbuf || NULL == cmask)
    {
        return 0;
    }
    Mat src(h,w,CV_8UC4,(unsigned char*) cbuf);
    Mat mask(h,w,CV_8UC4,(unsigned char*) cmask);

    cvtColor(src, src, CV_8UC3);
    cvtColor(mask, mask, CV_8UC1);

    Rect rect(Point(startX, startY), Point(currentX, currentY));

    switch(mode){
        case DRAW_RECT:
            grabCut(src, mask, rect, bgModel, fgModel, 3, GC_INIT_WITH_RECT);
            break;
        case DRAW_LINE:
            grabCut(src, mask, rect, bgModel, fgModel, 3, GC_INIT_WITH_MASK);
            break;
        default:
            break;
    }

    Mat foreGround(src.size(), CV_8UC3, Scalar(255, 255, 255));
    compare(mask, GC_PR_FGD, foreGround, CMP_EQ);

    int r = 0, g = 0, b = 0;
    for(int i = 0; i < src.rows; i++){
        for(int j = 0; j < src.cols; j++){
            r = foreGround.at<Vec3b>(i, j)[0];
            g = foreGround.at<Vec3b>(i, j)[1];
            b = foreGround.at<Vec3b>(i, j)[2];
            cbuf[i * foreGround.cols + j] = MAKE_RGBA(r, g, b, 255);
        }
    }

    //...这里写对Mat的操作就可以了
    //这里传回int数组。
    int size = w * h;
    jintArray  result = env->NewIntArray(size); //新建一个jintArray
    env->SetIntArrayRegion(result,0,size,cbuf); //将cbuf中的值复制到jintArray中去，数组copy
    env->ReleaseIntArrayElements(buf1,cbuf,0);  //复制变化，然后释放所有相关的资源
    env->ReleaseIntArrayElements(buf2,cmask,0);
    return result;
}