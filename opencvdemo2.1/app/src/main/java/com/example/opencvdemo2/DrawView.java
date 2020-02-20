package com.example.opencvdemo2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.compare;

/**
 * Created by wuwuwu on 2019/11/6 9:06
 */
public class DrawView extends View {

    private Paint paint = null; //画笔
    private Bitmap originalBitmap = null;
    private Bitmap new1Bitmap = null;
    private Bitmap new2Bitmap = null;
    private float clickX = 0,clickY = 0;    //点击位置
    private float startLineX = 0,startLineY = 0;    //直线开始位置
    private float startRectX = 0,startRectY = 0;    //矩形开始位置
    private boolean isMove = true;
    private int color = Color.GREEN;    //绘画颜色
    private float strokeWidth = 5.0f;   //画笔宽度
    private int mode = 0;   //grabCut的模式初始值为矩形
    private boolean show = false;   //显示结果

    public Mat mask;    //掩码蒙版
    public Mat binaryMask;  //二值化蒙版

    private static final int drawWithLinebg = 10;   //背景
    private static final int drawWithLinefg = 11;   //前景
    private static final int drawWithRect = 0;

    static {
        System.loadLibrary("native-lib");
    }

    public DrawView(Context context, Bitmap b)  //初始化
    {
        super(context);
        iniLoadOpencv();
        //以传入的Bitmap作为画布
        originalBitmap = Bitmap.createBitmap(b).copy(Bitmap.Config.ARGB_8888, true);
        new1Bitmap = Bitmap.createBitmap(originalBitmap);
        mask = new Mat(b.getHeight(), b.getWidth(), CvType.CV_8UC3);
    }

    public void setMode(int mode){
        this.mode = mode;
    }

    public void clear(){
        //清零
        startLineX = 0;
        startLineY = 0;
        startRectX = 0;
        startRectY = 0;
        clickX = 0;
        clickY = 0;
        show = false;
        new1Bitmap = Bitmap.createBitmap(originalBitmap);
        invalidate();
    }

    public void clearRect(){
        new1Bitmap = Bitmap.createBitmap(originalBitmap);
        invalidate();
    }

    public void setstyle(float strokeWidth){

        this.strokeWidth = strokeWidth;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if(mode == drawWithRect){
            clearRect();
        }
        if(show){
            canvas.drawBitmap(new2Bitmap, 0, 0, null);
        }
        else{
            canvas.drawBitmap(HandWriting(new1Bitmap), 0, 0,null);
        }


    }

    public Bitmap HandWriting(Bitmap originalBitmap)
    {
        Canvas canvas = null;

        canvas = new Canvas(originalBitmap);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        if(isMove){
            if(mode == drawWithLinebg){
                canvas.drawLine(startLineX, startLineY, clickX, clickY, paint);
                byte[] data = new byte[]{2, 2, 2};
                mask.put((int)clickX, (int)clickY, data);
                startLineX = clickX;    //更新位置，以极小的间隙逼近连续效果
                startLineY = clickY;
            }
            else if(mode == drawWithLinefg){
                canvas.drawLine(startLineX, startLineY, clickX, clickY, paint);
                byte[] data = new byte[]{3, 3, 3};
                mask.put((int)clickX, (int)clickY, data);
                startLineX = clickX;    //更新位置，以极小的间隙逼近连续效果
                startLineY = clickY;
            }
            else if (mode == drawWithRect){
                paint.setColor(Color.RED);
                canvas.drawRect(startRectX, startRectY, clickX, clickY, paint);
            }
        }

        return originalBitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        clickX = event.getX();
        clickY = event.getY();
        if(event.getAction() == MotionEvent.ACTION_DOWN){

            isMove = false;
            startRectX = clickX;
            startRectY = clickY;
            startLineX = clickX;
            startLineY = clickY;
            invalidate();
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE){

            isMove = true;
            invalidate();
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void guideFilter(){
        Bitmap bitmap = Bitmap.createBitmap(originalBitmap);
        Mat P = new Mat();
        Utils.bitmapToMat(bitmap, P);
        Imgproc.cvtColor(P, P, Imgproc.COLOR_BGR2GRAY); //转为灰度图
        P.convertTo(P, CvType.CV_64FC1, 1.0 / 255); //归一化
        int h = mask.rows(), w = mask.cols();
        int r = w / 10; //使用最小边的十分之一
        if (h < w){
            r = h / 10;
        }
        double eps = 0.000001;

        //下面的代码有问题，尝试改为bitmap的代码

        int[] I_ = new int[h * w];
        int[] P_ = new int[h * w];
        for (int i = 0; i < h; i++){
            for (int j = 0; j < w; j++){
                double[] data = mask.get(i, j);
                double[] pix = P.get(i, j);
                if ((int)data[0] == 3){
                    I_[i * w + j] = 1;
                }
                P_[i * w + j] = (int)pix[0];
            }
        }
        binaryMask = new Mat(h, w, CvType.CV_8UC1);
        int[] result = GuideFilter(I_, P_, r, eps, h, w);
        for (int i = 0; i < h; i++){
            for (int j = 0; j < w; j++){
                binaryMask.put(i, j, result[i * w + j]);
            }
        }
        P.release();
    }

    public void showFront(){
        //显示前景
        new2Bitmap = Bitmap.createBitmap(originalBitmap);
        Mat src = new Mat();   //将原图转为Mat类型
        Mat dst = new Mat();
        Mat result = new Mat();
        Mat fgModel = new Mat();
        Mat bgModel = new Mat();
        Utils.bitmapToMat(new2Bitmap, src); //转为Mat类型
        Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);
        Rect rect = new Rect(new Point((int)startRectX, (int)startRectY), new Point((int)clickX, (int)clickY));
        int spiltmode = Imgproc.GC_INIT_WITH_RECT;
        if(mode == drawWithLinebg || mode == drawWithLinefg){
            spiltmode = Imgproc.GC_INIT_WITH_MASK;
        }
        Imgproc.grabCut(src, mask, rect, bgModel, fgModel, 3, spiltmode);   //进行GrabCut操作
        guideFilter();  //进行引导滤波
        compare(binaryMask, new Scalar(1), result, 0);
        src.copyTo(dst, result);
        Utils.matToBitmap(dst, new2Bitmap);
        show = true;
        src.release();
        dst.release();
        bgModel.release();
        fgModel.release();
        result.release();
    }

    /**
     * 加载opencv库
     */
    private void iniLoadOpencv(){
        boolean success = OpenCVLoader.initDebug();
        if(success){
            Log.i("CV_TAG","opencv");
        }else{
            Toast.makeText(this.getContext(),"WARNING: not",Toast.LENGTH_SHORT).show();
        }
    }

    public static native int[] GuideFilter(int[] I, int[] P, int r, double eps, int h, int w);

}
