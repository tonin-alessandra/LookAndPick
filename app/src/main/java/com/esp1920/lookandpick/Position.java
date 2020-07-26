package com.esp1920.lookandpick;

import android.opengl.Matrix;

/**
 * This class represents a generic {@link Target} position.
 */
public class Position {
    private static final String TAG = "Position";

    private static final float MIN_TARGET_DISTANCE = 3.0f;
    private static final float MAX_TARGET_DISTANCE = 3.5f;

    private float[] mPosition;
    private float[] mModel;

    /**
     * Constructor.
     */
    Position(){
        mPosition = new float[]{0, 0, -MIN_TARGET_DISTANCE};
        mModel = new float[16];
    }

    /**
     * Constructor.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    Position(float x, float y, float z){
        mPosition = new float[]{x, y, z};
        mModel = new float[16];
    }

    /**
     * Gets the target position.
     * @return  The position of a {@link Target}.
     */
    public float[] getPosition() { return mPosition; }

    /**
     * Sets the target position.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param z The z coordinate.
     */
    public void setPosition(float x, float y, float z) {
        mPosition[0] = x;
        mPosition[1] = y;
        mPosition[2] = z;
    }

    /**
     * Sets the target position from a Position object.
     * @param position The Position object.
     */
    public void setPosition(Position position) {
        mPosition[0] = position.getXCoordinate();
        mPosition[1] = position.getYCoordinate();
        mPosition[2] = position.getYCoordinate();
    }

    /**
     * Gets the model.
     * @return  The model of a {@link Target}.
     */
    public float[] getModel() {
        updateModel();
        return mModel;
    }

    /**
     * Updates the model using the coordinates.
     */
    private void updateModel() {
        Matrix.setIdentityM(mModel, 0);
        Matrix.translateM(mModel, 0, mPosition[0], mPosition[1], mPosition[2]);
    }

    /**
     * Gets the x coordinate.
     * @return  The x coordinate.
     */
    public float getXCoordinate(){ return mPosition[0]; }

    /**
     * Gets the y coordinate.
     * @return  The y coordinate.
     */
    public float getYCoordinate(){ return mPosition[1]; }

    /**
     * Gets the z coordinate.
     * @return  The z coordinate.
     */
    public float getZCoordinate(){ return mPosition[2]; }
}
