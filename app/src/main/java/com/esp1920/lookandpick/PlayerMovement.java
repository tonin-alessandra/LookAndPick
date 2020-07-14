package com.esp1920.lookandpick;

import com.google.vr.sdk.base.HeadTransform;

/**
 * Class to handle user movement in the scene
 */
 public class PlayerMovement {

    /*
    * If the user tilts the cardboard viewer down with an angle greater
    * than THRESHOLD_ANGLE, the player will start moving
    * in the direction the head is looking at
    */
    private final float THRESHOLD_ANGLE = 15;
    private final float LIMIT_ANGLE = 90;

    private float[] rotationAngles;

    //walking speed
    private float speed;

    // Class contains static methods.
    public PlayerMovement() {}

    /**
     * Checks if the "walking conditions" are satisfied
     * TODO: write a better comment
     */
    public boolean isWalking(){
        HeadTransform head = new HeadTransform();
        head.getQuaternion(rotationAngles, 0);
        // rotationAngles = (x, y, z, w)

        if (rotationAngles[1] >= THRESHOLD_ANGLE && rotationAngles[1] <= LIMIT_ANGLE) {
            return true;
        }

        return false;
    }

    /**
     * Makes user walk forward -> makes the scene move backwards
     * TODO: write a better comment
     */
    public void walk(){
    }
}
