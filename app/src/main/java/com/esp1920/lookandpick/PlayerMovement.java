package com.esp1920.lookandpick;

import android.opengl.Matrix;
import android.widget.Toast;

import com.google.vr.sdk.base.HeadTransform;

/**
 * Class to handle user movement in the scene
 *
 * If the user tilts the cardboard viewer down with a certain angle, the player will take a step
 * forward in the direction the head is looking at.
 * If the user tilts the cardboard viewer up with a certain angle, the player will move backwards.
 *
 */
 public class PlayerMovement {

    private final double THRESHOLD_ANGLE = 30; // degrees

    private float[] eulerAngles;
    private float[] forwardVec;

    private double pitch, yaw, roll;

    //walking speed
    private float speed;


    public PlayerMovement() {
        eulerAngles = new float[4];
        forwardVec = new float[3];
        speed = 1.0f;
    }

    /**
     * Checks if the "walking conditions" are satisfied
     * If the user tilts the viewer (up or down) with an angle greater than THRESHOLD_ANGLE
     * it will move accordingly.
     */
    public boolean isWalking(HeadTransform headTransform){
        headTransform.getEulerAngles(eulerAngles, 0);
        pitch = Math.toDegrees(eulerAngles[0]); // Y
        yaw = Math.toDegrees(eulerAngles[1]); // Z
        roll = Math.toDegrees(eulerAngles[2]); // X

        if(Math.abs(pitch) >= THRESHOLD_ANGLE){
            return true;
        }

        return false;
    }

    /**
     * Calculates eyes's coordinates in order to achieve movement
     *
     */
    public void walk(HeadTransform headTransform, float[] eyePosition) {
        headTransform.getForwardVector(forwardVec, 0);
        headTransform.getEulerAngles(eulerAngles, 0);

        eyePosition[0] = forwardVec[0]*speed;
        eyePosition[1] = 0;
        eyePosition[2] = forwardVec[2]*speed;
    }


}
