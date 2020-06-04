package com.esp1920.lookandpick;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

public class Target {
    private final String TAG = "Target";
    private String mFilePath;

    private float[] mPosition;
    private float[] mModelTarget;

    private String mName;
    // TODO: create Category.java to manage categories
    private Category mCategory;
    private int mScore;

    private TexturedMesh mTexturedMesh;
    private Texture mNotSelectedTexture;
    private Texture mSelectedTexture;

    public Target(Context context, int objectPositionParam, int objectUvParam,
                  String filePath, String selectedTexture, String notSelectedTexture){
        setFilePath(filePath);
        try {
            mTexturedMesh = new TexturedMesh(context, filePath, objectPositionParam, objectUvParam);
            mNotSelectedTexture = new Texture(context, notSelectedTexture);
            mSelectedTexture = new Texture(context, selectedTexture);
        }catch(IOException e){
            Log.e(TAG, "Unable to initializa objects", e);
        }
    }

    public Target(String name, int score, Category category){
        mPosition = new float[3];
        mModelTarget = new float[16];
        setName(name);
        setScore(score);
        setCategory(category);
    }

    public float[] getModelTarget() {
        return mModelTarget;
    }

    public void setModelTarget(float[] modelTarget) {
        mModelTarget = modelTarget;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Category getCategory() {
        return mCategory;
    }

    public void setCategory(Category category) {
        mCategory = category;
    }

    public int getScore() {
        return mScore;
    }

    public void setScore(int score) {
        mScore = score;
    }

    public TexturedMesh getTexturedMesh() {
        return mTexturedMesh;
    }

    public void setTexturedMesh(TexturedMesh texturedMesh) {
        mTexturedMesh = texturedMesh;
    }

    public Texture getNotSelectedTexture() {
        return mNotSelectedTexture;
    }

    public void setNotSelectedTexture(Texture notSelectedTexture) {
        mNotSelectedTexture = notSelectedTexture;
    }

    public Texture getSelectedTexture() {
        return mSelectedTexture;
    }

    public void setSelectedTexture(Texture selectedTexture) {
        mSelectedTexture = selectedTexture;
    }

    public float[] getPosition() {
        return mPosition;
    }

    public void setPosition(float x, float y, float z) {
        mPosition[0] = x;
        mPosition[1] = y;
        mPosition[2] = z;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    public String getTAG() {
        return TAG;
    }
}
