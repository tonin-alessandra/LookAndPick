package com.esp1920.lookandpick;

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

    private final double THRESHOLD_ANGLE = 40; // degrees

    private float[] eulerAngles;

    private double pitch, yaw, roll;

    //walking speed
    private float speed;

    private float inc;

    public PlayerMovement() {
        eulerAngles = new float[4];
        speed = 0.03f;
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
    public void walk(float[] eyePosition) {
        inc = -.020f;
        if (yaw >= -90 && yaw <= 90) {  // 180 degrees swivel
            //eyePosition[1] = (float)(Math.PI * 2 * yaw * inc);
        }
        eyePosition[2] -= speed;
    }
}

   /* @Override
    public void headTransform(HeadTransform headTransform) {
        headTransform.getQuaternion(quat, 0);
        headTransform.getEulerAngles(euler, 0);
        bank = degrees(euler[0]);
        heading = degrees(euler[1]);
        attitude = degrees(euler[2]);
        float zoomSpeedMax = .01f;  //.01f;
        float TILT_FOR_ZOOM = 7.5f; // degrees
        float inc = -.020f;
        // X
        if (heading >= -90 && heading <= 90) {  // 180 degrees swivel
            cameraPositionX = PI * 2 * heading * inc;
        }
        // Y
        inc = .04f;
        if (bank >= -90 && bank <= 90) {  // 180 degrees swivel
            cameraPositionY = -2 * bank * inc;
        }
        // Z
        inc = 0.0f;
        if (attitude > TILT_FOR_ZOOM) {
            inc = zoomSpeedMax;
        } else if (attitude < -TILT_FOR_ZOOM) {
            inc = -zoomSpeedMax;
        }
        cameraPositionZ += inc;
    }*/