package com.esp1920.lookandpick;

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

    private final int FORWARD = 1;
    private final int BACKWARD = -1;
    private final int STILL = 0;

    private float[] eulerAngles;
    private float[] forwardVec;

    private double pitch, yaw, roll;
    private double prevYaw;

    private int direction;
    private int prevDirection;

    private float eyeZ;

    private float step;
    private float speed;

    public PlayerMovement() {
        eulerAngles = new float[4];
        forwardVec = new float[3];
        step = 0.25f;
        speed = 0.009f;
        prevYaw = 0.0f;
        prevDirection = -2;
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
        //roll = Math.toDegrees(eulerAngles[2]); // X

        if (pitch <= THRESHOLD_ANGLE * -1) { return FORWARD; }

        if(pitch >= THRESHOLD_ANGLE){ return BACKWARD; }

        return STILL;
    }

    /**
     * Calculates eyes's coordinates in order to achieve movement
     *
     */
    public String updateEyePosition (HeadTransform headTransform, float[] eyePosition) {
        direction = isWalking(headTransform);
        if(direction == STILL) return "";

        headTransform.getForwardVector(forwardVec, 0);
        eyeZ = eyePosition[2];
        if(direction == FORWARD && canGoForward()){
                eyePosition[2] = (eyeZ + nextStep()) * forwardVec[2];
        }
        if(direction == BACKWARD && canGoBackwards()){
                eyePosition[2] = (eyeZ + nextStep()) * forwardVec[2] * -1;
        }
        return  //"dir: "+String.valueOf(direction)+
                // "pitch: "+String.valueOf(eulerAngles[0])+"yaw: "+String.valueOf(eulerAngles[1])
                "eyeX : "+String.valueOf(eyePosition[0])+" eyeY: "+String.valueOf(eyePosition[1])+
                " eyeZ: "+String.valueOf(eyePosition[2]);
    }

    private float nextStep(){
        /*if (Math.abs(yaw - prevYaw) >= 2 ){
            prevYaw = yaw;
            return step = 0.25f;
        }*/

        if(direction != prevDirection){
            prevDirection = direction;
            return step = (eyeZ * 2 - 0.25f) * -1;
        }

        return step += speed;
    }

    private boolean canGoForward(){
        if (eyeZ < -0.9f) {
            return false;
        }
        return true;
    }

    private boolean canGoBackwards(){
        if (eyeZ > 6.5f) {
            return false;
        }
        return true;
    }
}
