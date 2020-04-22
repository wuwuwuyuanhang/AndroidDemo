package com.example.tensorflowtest.view;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tensorflowtest.R;
import com.example.tensorflowtest.tflite.Classifier;
import com.example.tensorflowtest.tflite.Result;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String mModelPath = "mnist_savedmodel.tflite";

    private FrameLayout mImage;
    private DrawView drawView;
    private TextView numberText;
    private TextView probabilityText;
    private Button run;
    private Button clear;

    private Classifier classifier;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            init();
            initListener();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        run = (Button)findViewById(R.id.runModel);
        clear = (Button)findViewById(R.id.clearCanvas);
        numberText = (TextView)findViewById(R.id.showResult);
        probabilityText = (TextView)findViewById(R.id.showProbability);
        mImage = (FrameLayout) findViewById(R.id.srcImage);
        drawView = new DrawView(MainActivity.this);
        mImage.addView(drawView);

        classifier = new Classifier(this.getAssets(), mModelPath);
    }

    private void initListener(){
        run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = drawView.getBitmap();
                Result result = classifier.recognizeImage(bitmap);
                numberText.setText(String.valueOf(result.getNumber()));
                probabilityText.setText(String.valueOf(result.getProbability()));
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clear();
                numberText.setText("--");
                probabilityText.setText("--");
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mImage.removeView(drawView);
    }
}
