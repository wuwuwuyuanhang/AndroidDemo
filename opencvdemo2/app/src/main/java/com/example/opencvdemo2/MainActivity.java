package com.example.opencvdemo2;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

import static org.opencv.core.Core.compare;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button mRect;
    private Button mLinefg;
    private Button mShow;
    private Button mLinebg;
    private LinearLayout mImage;
    private Bitmap mbitmap;
    private DrawView mdrawView;
    private Paint mpaint;   //画笔

    private int mode;

    private static final int drawWithLinebg = 10;   //背景
    private static final int drawWithLinefg = 11;   //前景
    private static final int drawWithRect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iniLoadOpencv();
        init();
        setLinster();

    }

    private void init() {
        mRect = (Button)findViewById(R.id.rect);
        mLinefg = (Button)findViewById(R.id.linefg);
        mLinebg = (Button)findViewById(R.id.linebg);
        mShow = (Button)findViewById(R.id.showFront);
        mImage = (LinearLayout) findViewById(R.id.srcImage);

        mbitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2);

        mdrawView = new DrawView(MainActivity.this, mbitmap);
        mImage.addView(mdrawView);


    }

    private void setLinster(){
        mRect.setOnClickListener(this);
        mLinefg.setOnClickListener(this);
        mShow.setOnClickListener(this);
        mLinebg.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rect:
                mdrawView.clear();
                mode = drawWithRect;
                mdrawView.setMode(mode);
                break;
            case R.id.linebg:
                mdrawView.clear();
                mode = drawWithLinebg;
                mdrawView.setMode(mode);
                break;
            case R.id.linefg:
                mdrawView.clear();
                mode = drawWithLinefg;
                mdrawView.setMode(mode);
                break;
            case R.id.showFront:
                mdrawView.showFront();
                break;
        }
    }
}
