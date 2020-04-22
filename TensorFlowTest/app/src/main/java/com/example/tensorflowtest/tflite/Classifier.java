package com.example.tensorflowtest.tflite;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by wuwuwu on 2020/4/18.
 */
public class Classifier {

    private static final int BATCH_SIZE = 1;
    private static final int INPUT_HEIGHT = 28;
    private static final int INPUT_WIDTH = 28;
    private static final int INPUT_CHANNELS = 1;
    private static final int MAX_RESULTS = 10;

    private final Interpreter interpreter;
    private final Interpreter.Options options = new Interpreter.Options();
    private final float[][] mResult = new float[1][MAX_RESULTS];

    private static final String TAG = "Classifier";

    public Classifier(AssetManager assetManager, String modelPath) throws IOException {
        // 修改解析器的线程
        options.setNumThreads(5);
        // 打开神经网络API，启动硬件加速
        options.setUseNNAPI(true);
        interpreter = new Interpreter(loadModelFile(assetManager, modelPath), options);
    }

    // 加载训练模型
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        // 加载模型
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        // 文件描述传递到文件流入流对象
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        // Channel、startOffset、declaredLength
        FileChannel fileChannel = inputStream.getChannel();
        Long startOffset = fileDescriptor.getStartOffset();
        Long declaredLengths = fileDescriptor.getDeclaredLength();
        // 映射该文件通道以读取模型的原始字节
        return  fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLengths);
    }

    // 识别结果
    public Result recognizeImage(Bitmap bitmap){
        // 图片转为输入大小
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_WIDTH, INPUT_HEIGHT, false);
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(scaledBitmap);
        // 1 * 10 数组，有10个label
        // 运行结束后，每个二级元素都是一个label的概率
        interpreter.run(byteBuffer, mResult);
        return new Result(mResult[0]);
    }

    // 将输入的Bitmap转化为Interpreter可以识别的ByteBuffer
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * INPUT_HEIGHT * INPUT_WIDTH * INPUT_CHANNELS);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_HEIGHT * INPUT_WIDTH];

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < INPUT_WIDTH; i++){
            for (int j = 0; j < INPUT_HEIGHT; j++){
                int input = intValues[pixel++];
                float r = input >> (16) & 0xFF;
                float g = input >> (8) & 0xFF;
                float b = input & 0xFF;
                float gray = (255.0f - (r * 0.2126f + g * 0.7152f + b * 0.722f)) / 255.0f;
                if (gray > 1.0f){
                    gray = 1.0f;
                }
                else if (gray < 0){
                    gray = 0.0f;
                }
                Log.d(TAG, "convertBitmapToByteBuffer: " + i + j + gray);
                byteBuffer.putFloat(gray);
            }
        }
        return byteBuffer;
    }

}
