package com.esp1920.lookandpick;

import android.opengl.Matrix;

import java.util.Random;

/**
 * This class represents a generic {@link Target} position.
 */
public class Position {
    private static final String TAG = "Position";

    // The maximum yaw and pitch of the target object, in degrees. After hiding the target, its
    // yaw will be within [-MAX_YAW, MAX_YAW] and pitch will be within [-MAX_PITCH, MAX_PITCH].
    private static final float MAX_YAW = 100.0f;
    private static final float MAX_PITCH = 25.0f;

    private static final float MIN_TARGET_DISTANCE = 3.0f;
    private static final float MAX_TARGET_DISTANCE = 7.0f;

    private float[] mPosition;
    private float[] mModel;

    private Random random;

    /**
     * Constructor.
     */
    Position(){
        mPosition = new float[]{0, 0, -MIN_TARGET_DISTANCE};
        mModel = new float[16];
        random = new Random();
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
        random = new Random();
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

    /**
     *  Generates random position.
     */
    public void generateRandomPosition(){
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];

        // Matrix.setRotateM takes the angle in degrees, but Math.tan takes the angle in radians, so
        // yaw is in degrees and pitch is in radians.
        float yawDegrees = (random.nextFloat() - 0.5f) * 2.0f * MAX_YAW;
        float pitchRadians = (float) Math.toRadians((random.nextFloat() - 0.5f) * 2.0f * MAX_PITCH);

        // Creates a matrix for rotation by angle yawDegrees around the y axis.
        Matrix.setRotateM(rotationMatrix, 0, yawDegrees, 0.0f, 1.0f, 0.0f);

        // Calculates a new random position
        float targetDistance =
                random.nextFloat() * (MAX_TARGET_DISTANCE - MIN_TARGET_DISTANCE) + MIN_TARGET_DISTANCE;

        Position temp;
        // Allows to generate objects with negative z coordinate randomly.
        if(random.nextBoolean())
            temp = new Position(0,0, -targetDistance);
        else
            temp = new Position(0,0, targetDistance);

        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, temp.getModel(), 12);


        setPosition(posVec[0], (float) Math.tan(pitchRadians) * temp.getZCoordinate(), posVec[2]);
    }
}
