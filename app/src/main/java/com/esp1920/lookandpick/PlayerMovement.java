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

    private final double THRESHOLD_ANGLE = 35; // degrees

    private float[] eulerAngles;
    private float[] forwardVec;

    private double pitch, yaw, roll;

    private double oldYaw;

    //walking speed
    private float step;

    public PlayerMovement() {
        eulerAngles = new float[4];
        forwardVec = new float[3];
        step = 0.3f;
        oldYaw = 0.0f;
    }

    /**
     * Checks if the "walking conditions" are satisfied
     * If the user tilts the viewer (up or down) with an angle greater than THRESHOLD_ANGLE
     * it will move accordingly.
     */
    private int isWalking(HeadTransform headTransform){
        headTransform.getEulerAngles(eulerAngles, 0);
        pitch = Math.toDegrees(eulerAngles[0]); // Y
        yaw = Math.toDegrees(eulerAngles[1]); // Z
        roll = Math.toDegrees(eulerAngles[2]); // X

        //TODO: non int ma define
        if(pitch <= THRESHOLD_ANGLE*-1){ return 1; }

        if(pitch >= THRESHOLD_ANGLE){ return 2; }

        return 0;
    }

    /**
     * Calculates eyes's coordinates in order to achieve movement
     *
     */
    public String updateEyePosition (HeadTransform headTransform, float[] eyePosition) {
        int a = isWalking(headTransform);
        if(a == 0) return "";

        headTransform.getForwardVector(forwardVec, 0);
        headTransform.getEulerAngles(eulerAngles, 0);

        yaw = eulerAngles[1];
        if (Math.abs(yaw - oldYaw) >= 30 ){ step = 0.3f; }

        if(a == 2){
            eyePosition[2] = forwardVec[2] * -1 * nextStep();
        }
        if(a == 1){
            if (eyePosition[2] < -0.7 && Math.abs(yaw) < 20){ return "stop"; }
            eyePosition[2] = forwardVec[2] * 0.8f * nextStep();
        }
        oldYaw = yaw;
        return  "dir: "+String.valueOf(a)+
                "x: "+String.valueOf(forwardVec[0])+"y: "+String.valueOf(forwardVec[1])
                +"z: "+String.valueOf(forwardVec[2])+"eye: "+String.valueOf(eyePosition[2]);



    }

    private float nextStep(){
        return step += 0.05f;
    }




}
