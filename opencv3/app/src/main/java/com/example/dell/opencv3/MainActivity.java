package com.example.dell.opencv3;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;

import static org.opencv.core.Core.compare;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    static {
        System.loadLibrary("native-lib");
    }

    public native int[] splitImage(int[] buf1, int[] buf2, int w, int h, int x1, int y1, int x2, int y2, int mode);

    private Button mRect;
    private Button mLinefg;
    private Button mShow;
    private Button mLinebg;
    private ImageView mImage;
    private ImageView mshowImage;
    private boolean paintRect, paintMask;

    public int startX, startY;
    public int currentX, currentY;

    public int splitMode;
    public int mode;    //3：前景；2：背景

    public Point a, b;
    public ArrayList<Point> mCircle = new ArrayList<Point>();

    public Mat mask;

    public Bitmap mBitmap1;
    public Bitmap mMask;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniLoadOpencv();
        init();
        setListener();

    }

    private void init() {
        mRect = (Button)findViewById(R.id.rect);
        mLinefg = (Button)findViewById(R.id.linefg);
        mLinebg = (Button)findViewById(R.id.linebg);
        mShow = (Button)findViewById(R.id.showFront);
        mImage = (ImageView)findViewById(R.id.image);
        mshowImage = (ImageView)findViewById(R.id.showImage);

        mBitmap1 = ((BitmapDrawable) ((ImageView) mImage).getDrawable()).getBitmap();
        /*mMask = ((BitmapDrawable) ((ImageView) mImage).getDrawable()).getBitmap();
        int[] pixels = new int [mMask.getWidth() * mMask.getHeight()];
        Arrays.fill(pixels, 3);
        mMask.setPixels(pixels, 0, mMask.getWidth(), 0, 0, mMask.getWidth(), mMask.getHeight());*/
        mask = new Mat();
        mask.create(mBitmap1.getHeight(), mBitmap1.getWidth(), CvType.CV_8UC3);

        mode = 3;
        paintRect = false;
        paintMask = false;
    }

    private void setListener() {
        mRect.setOnClickListener(this);
        mLinefg.setOnClickListener(this);
        mLinebg.setOnClickListener(this);
        mShow.setOnClickListener(this);

        mImage.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:   //手指按下
                startX = (int) motionEvent.getX();
                startY = (int) motionEvent.getY();
                mCircle.clear();
                mCircle.add(new Point(startX, startY));
                showToast("起始位置：(" + startX + "," + startY);
                break;
            case MotionEvent.ACTION_MOVE:   //手指滑动
                currentX = (int) motionEvent.getX();
                currentY = (int) motionEvent.getY();
                showToast("实时位置：(" + currentX + "," + currentY);
                mCircle.add(new Point(currentX, currentY));
                showPaintedImagge();
                break;
            case MotionEvent.ACTION_UP: //手指释放
                currentX = (int) motionEvent.getX();
                currentY = (int) motionEvent.getY();
                showToast("结束位置：(" + currentX + "," + currentY);
                mCircle.add(new Point(currentX, currentY));
                showPaintedImagge();
                break;
            default:
                break;
        }
        /**
         *  注意返回值
         *  true：view继续响应Touch操作；
         *  false：view不再响应Touch操作，故此处若为false，只能显示起始位置，不能显示实时位置和结束位置
         */
        return true;
    }

    public void showToast(String m){
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId()){
            case R.id.rect:
                //选择矩形框
                paintRect = true;
                paintMask = false;
                splitMode = Imgproc.GC_INIT_WITH_RECT;
                break;
            case R.id.linefg:
                //选择直线
                paintRect = false;
                paintMask = true;
                splitMode = Imgproc.GC_INIT_WITH_MASK;
                mode = 3;
                break;
            case R.id.linebg:
                paintRect = false;
                paintMask = true;
                splitMode = Imgproc.GC_INIT_WITH_MASK;
                mode = 2;
                break;
            case R.id.showFront:
                //显示前景
                /*int width = mBitmap1.getWidth();
                int height = mBitmap1.getHeight();
                int[] pix = new int[width * height];
                mBitmap1.getPixels(pix, 0, mBitmap1.getWidth(), 0, 0 , width, height);
                int[] pix1 = new int[width*height];
                mMask.getPixels(pix1, 0, mMask.getWidth(), 0, 0, width, height);
                int[] resultPixes =  splitImage(pix, pix1, width, height, startX, startY, currentX, currentY, splitMode);
                Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                result.setPixels(resultPixes, 0, width, 0, 0, width, height);
                mshowImage.setImageBitmap(result);*/

                Bitmap bitmap1 = mBitmap1.copy(Bitmap.Config.ARGB_8888, true);
                Mat src1 = new Mat();
                Mat dst1 = new Mat();
                Mat result1 = new Mat();
                Mat fgModel1 = new Mat();
                Mat bgModel1 = new Mat();
                Utils.bitmapToMat(mBitmap1, src1);
                Imgproc.cvtColor(src1, src1, Imgproc.COLOR_BGRA2BGR);
                Rect rect1 = new Rect(new Point(startX, startY), new Point(currentX, currentY));
                Imgproc.grabCut(src1, mask, rect1, bgModel1, fgModel1, 3, splitMode);
                compare(mask, new Scalar(3), result1, 0);
                src1.copyTo(dst1, result1);
                Utils.matToBitmap(dst1, bitmap1);
                mshowImage.setImageBitmap(bitmap1);
                src1.release();
                dst1.release();
                result1.release();
                fgModel1.release();
                bgModel1.release();

                break;
            default:
                break;
        }
    }


    /**
     * 加载opencv库
     */
    private void iniLoadOpencv(){
        boolean success = OpenCVLoader.initDebug();
        if(success){
            Log.i("CV_TAG","opencv");
        }else{
            Toast.makeText(this.getApplicationContext(),"WARNING: not",Toast.LENGTH_SHORT).show();
        }
    }

    public void showPaintedImagge(){

        a = new Point(startX, startY);
        b = new Point(currentX, currentY);
        if(paintRect && !paintMask){
            Mat src = new Mat();
            Bitmap bitmap = mBitmap1.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bitmap, src);
            Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);
            Imgproc.rectangle(src, a, b, new Scalar(0, 0, 255), 10);
            Utils.matToBitmap(src, bitmap);
            mImage.setImageBitmap(bitmap);
            src.release();
        }
        if(!paintRect && paintMask){
            Mat src = new Mat();
            Bitmap bitmap = mBitmap1.copy(Bitmap.Config.ARGB_8888, true);;
            Utils.bitmapToMat(bitmap, src);
            Imgproc.cvtColor(src, src, Imgproc.COLOR_BGRA2BGR);
            int index = 0;
            while (index < mCircle.size()){
                Point current = mCircle.get(index++);
                Imgproc.circle(src, current, 5, new Scalar(255, 0, 0), -1, 8, 2);
                if(mode == 3){
                    byte[] data = new byte[]{3, 3, 3};
                    mask.put((int)current.x, (int)current.y, data);
                }
                else if(mode == 2){
                    byte[] data = new byte[]{2, 2, 2};
                    mask.put((int)current.x, (int)current.y, data);
                }
            }
            Utils.matToBitmap(src, bitmap);
            mImage.setImageBitmap(bitmap);
            src.release();
        }
    }

    @Override
    protected void onResume() {
        mask.release();
        super.onResume();
    }
}

