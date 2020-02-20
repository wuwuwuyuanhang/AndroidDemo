/**
 * Created by wuwuwu on 2020/2/20.
 */
//
#include "GuideFilter.h"

Mat cumsum(Mat& src, int rc) {
    int cols = src.cols;
    int rows = src.rows;
    Mat mat = Mat::zeros(rows, cols, CV_64FC1);
    double start = 0;
    if (rc == 1) {
        for (int i = 0; i < cols; i++) {
            start = 0;
            for (int j = 0; j < rows; j++) {
                start = start + src.at<double>(j, i);
                mat.at<double>(j, i) = start;
            }
        }
    }
    else if (rc == 2) {
        for (int i = 0; i < rows; i++) {
            start = 0;
            for (int j = 0; j < cols; j++) {
                start = start + src.at<double>(i, j);
                mat.at<double>(i, j) = start;
            }
        }
    }
    return mat;
}

Mat boxfilter(Mat& src, int r) {
    int cols = src.cols;
    int rows = src.rows;
    Mat dest = Mat::zeros(rows, cols, CV_64FC1);
    Mat temp = cumsum(src, 1);
    int i = 0;
    int j = 0;
    for (; i < r + 1; i++) {
        for (j = 0; j < cols; j++) {
            dest.at<double>(i, j) = temp.at<double>(i + r, j);
        }
    }
    for (; i < rows - r; i++){
        for (j = 0; j < cols; j++) {
            dest.at<double>(i, j) = temp.at<double>(i + r, j) - temp.at<double>(i - r - 1, j);
        }
    }
    for (; i < rows; i++) {
        for (j = 0; j < cols; j++) {
            dest.at<double>(i, j) = temp.at<double>(rows - 1, j) - temp.at<double>(i - r - 1, j);
        }
    }
    temp = cumsum(dest, 2);
    for (i = 0; i < rows; i++) {
        for (j = 0; j < r + 1; j++) {
            dest.at<double>(i, j) = temp.at<double>(i , j + r);
        }
    }
    for (i = 0; i < rows; i++) {
        for (j = r + 1; j < cols - r; j++) {
            dest.at<double>(i, j) = temp.at<double>(i, j + r) - temp.at<double>(i, j - r - 1);
        }
    }
    for (i = 0; i < rows; i++) {
        for (j = cols - r; j < cols; j++) {
            dest.at<double>(i, j) = temp.at<double>(i, cols - 1) - temp.at<double>(i, j - r - 1);
        }
    }
    return dest;
}


Mat guidefilter(Mat& I, Mat& P, int r, double eps) {
    int rows = I.rows;
    int cols = I.cols;
    Mat n = Mat::ones(rows, cols, CV_64FC1);
    n = boxfilter(n, r);
    Mat meanI = boxfilter(I, r) / n;
    Mat meanP = boxfilter(P, r) / n;
    Mat meanIP = I.mul(P);
    meanIP = boxfilter(meanIP, r) / n;
    Mat covIP = meanIP - meanI.mul(meanP);
    Mat meanII = I.mul(I);
    meanII = boxfilter(meanII, r) / n;
    Mat varI = meanII - meanI.mul(meanI);
    Mat a = covIP / (varI + eps);
    Mat b = meanP - a.mul(meanI);
    Mat meanA = boxfilter(a, r) / n;
    Mat meanB = boxfilter(b, r) / n;
    return meanA.mul(I)+ meanB;
}