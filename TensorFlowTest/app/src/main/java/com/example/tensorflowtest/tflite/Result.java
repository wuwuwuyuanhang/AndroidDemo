package com.example.tensorflowtest.tflite;

/**
 * Created by wuwuwu on 2020/4/20.
 */
public class Result {
    private int mNumber = 0;
    private float mProbability = 0f;

    public Result(float[] probs){
        mNumber = argmax(probs);
        mProbability = probs[mNumber];
    }

    public int getNumber(){
        return mNumber;
    }

    public float getProbability(){
        return mProbability;
    }

    private static int argmax(float[] probs){
        int maxIndex = -1;
        float maxProbability = 0.0f;
        for (int i = 0; i < probs.length; i++){
            if (probs[i] > maxProbability){
                maxIndex = i;
                maxProbability = probs[i];
            }
        }
        return maxIndex;
    }
}
