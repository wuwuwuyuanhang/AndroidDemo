/**
 * Created by wuwuwu on 2020/2/19.
 */
//

#ifndef OPENCVDEMO2_GUIDEFILTER_H
#define OPENCVDEMO2_GUIDEFILTER_H

#endif //OPENCVDEMO2_GUIDEFILTER_H

#include "opencv2/opencv.hpp"
#include <iostream>

using namespace cv;
using namespace std;

Mat cumsum(Mat& src, int rc);
Mat boxfilter(Mat& src, int r);
Mat guidefilter(Mat& I, Mat& p, int r, double eps);