package com.example.tensorflowtest.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wuwuwu on 2020/4/19.
 */
public class DrawView extends View {

    private Paint paint = null; //画笔
    private Path path = null;
    private Canvas canvas = null;
    private Bitmap showBitmap = null;
    private float clickX = 0, clickY = 0;    //点击位置
    private float preX = 0, preY = 0;    //直线开始位置
    private int color = Color.BLACK;    //绘画颜色
    private float strokeWidth = 40.0f;   //画笔宽度
    private int height = 1080, width = 1080;

    public DrawView(Context context)  //初始化
    {
        super(context);
        //以传入的Bitmap作为画布
        showBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        showBitmap.eraseColor(Color.parseColor("#FFFFFF"));

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);

        path = new Path();
        canvas = new Canvas(showBitmap);
    }

    public void setstyle(float strokeWidth){
        this.strokeWidth = strokeWidth;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        canvas.drawBitmap(showBitmap, 0, 0,paint);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        clickX = event.getX();
        clickY = event.getY();
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            path.moveTo(clickX, clickY);
            preX = clickX;
            preY = clickY;
            invalidate();
            return true;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE){
            path.quadTo(preX, preY, (clickX + preX) / 2, (clickY + preY) / 2);
            preX = clickX;
            preY = clickY;
            canvas.drawPath(path, paint);
            invalidate();
            return true;
        }
        else if (event.getAction() == MotionEvent.ACTION_UP){
            path.reset();
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public Bitmap getBitmap(){
        return showBitmap;
    }

    public void clear(){
        //清零
        preX = 0;
        preY = 0;
        clickX = 0;
        clickY = 0;
        showBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        showBitmap.eraseColor(Color.parseColor("#FFFFFF"));
        canvas.setBitmap(showBitmap);
        path.reset();
        invalidate();
    }

}
