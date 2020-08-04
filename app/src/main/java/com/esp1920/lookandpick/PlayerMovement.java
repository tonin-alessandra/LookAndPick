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

    private static final double THRESHOLD_ANGLE = 30; // degrees

    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;
    private static final int STILL = 0;
    private int prevDirection = -2;
    private int direction;

    private static final int AHEAD = -1;
    private static final int BEHIND = 1;
    private int prevOrientation = 0;
    private int orientation;

    private float[] eulerAngles = new float[4];
    private float[] forwardVec = new float[3];

    private double pitch;

    private float newEyeZ;

    private float step = 0.25f;
    private float speed = 0.009f;
    //speed moving towards the wall ahead (purple tiles)
    //private float speedB = 0.006f; // speed moving towards the wall behind (blue tiles)

    private boolean canMoveForward;
    private boolean canMoveBackward;

    public PlayerMovement() {}

    /**
     * Checks if the "walking conditions" are satisfied
     * If the user tilts the viewer (up or down) with an angle greater than THRESHOLD_ANGLE
     * it will move accordingly.
     */
    private int isWalking(float[] eulerAngles){
        pitch = Math.toDegrees(eulerAngles[0]); // Y

        if (pitch <= THRESHOLD_ANGLE * -1) { return FORWARD; }

        if(pitch >= THRESHOLD_ANGLE){ return BACKWARD; }

        return STILL;
    }

    /**
     * Calculates eyes's coordinates in order to achieve movement
     *
     */
    public float updateEyePosition (HeadTransform headTransform, float eyeZ) {
        headTransform.getForwardVector(forwardVec, 0);
        headTransform.getEulerAngles(eulerAngles, 0);
        orientation = (int)Math.signum(forwardVec[2]);
        direction = isWalking(eulerAngles);
        newEyeZ = eyeZ;
        checkBounds();
        if(direction == FORWARD && canMoveForward){
                newEyeZ = (eyeZ + nextStep()) * forwardVec[2];
        }
        else if(direction == BACKWARD && canMoveBackward){
                newEyeZ = (eyeZ + nextStep()) * forwardVec[2] * -1;
        }
        return newEyeZ;
    }

    private float nextStep(){
        if(direction != prevDirection){
            prevDirection = direction;
            if (orientation == AHEAD){
                if(direction == FORWARD ){
                    return step = (newEyeZ * 2 - 0.25f) * -1;
                }
                if(direction == BACKWARD){
                    return step = 0.25f;
                }
            }
            if (orientation == BEHIND){
                if(direction == FORWARD ){
                    return step = 0.25f;
                }
                if(direction == BACKWARD){
                    return step = (newEyeZ * 2 - 0.25f) * -1;
                }
            }
        }
        if(orientation != prevOrientation){
            prevOrientation = orientation;
            return step = 0.25f; //TODO: non va bene praticamente mai
        }
        return step += speed;
    }

    private void checkBounds(){
        if ((orientation == AHEAD && newEyeZ < -0.9f) || (orientation == BEHIND && newEyeZ > 5.5f)) {
            canMoveForward = false;
        } else{
            canMoveForward = true;
        }
        if ((orientation == AHEAD && newEyeZ > 5.5f) || (orientation == BEHIND && newEyeZ < -0.9f)) {
            canMoveBackward = false;
        } else{
            canMoveBackward = true;
        }
    }
}
