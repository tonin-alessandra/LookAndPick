package com.esp1920.lookandpick;

import com.google.vr.sdk.base.HeadTransform;


/**
 * Class to handle user movement along Z axis.
 *
 * If the user tilts the cardboard viewer down with an angle greater than the threshold, the player
 * will start moving forward in the direction the head is looking at.
 * If the user tilts the cardboard viewer up with an angle greater than the threshold, the player
 * will move backwards.
 * Movement is enabled only if the user is looking at the wall with purple tiles or the wall with
 * blue tiles, these are, respectively, the wall ahead and the wall behind when the game begins.
 *
 * This feature is meant to be tested using a Cardboard viewer.
 *
 */
 public class PlayerMovement {
    private final String TAG = "PlayerMovement";

    private static final double THRESHOLD_ANGLE = 30; // degrees

    // direction represents the type of movement, forward or backward, depending on the viewer tilt
    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;
    private static final int STILL = 0;
    private int prevDirection = -2;
    private int direction;

    //orientation represent the wall the user is facing
    private static final int AHEAD = -1;
    private static final int BEHIND = 1;
    private int prevOrientation = 0;
    private int orientation;

    private float[] eulerAngles = new float[4];
    private float[] forwardVec = new float[3];

    // movement around Y axis
    private double pitch;

    private float newEyeZ;

    private static final float START = 0.25f;
    private static final float SPEED = 0.03f;
    private float step;

    private float compensation = 0.65f;

    private boolean canMoveForward;
    private boolean canMoveBackward;
    private static final float boundA = -0.9f;
    private static final float boundB = 5.5f;

    public PlayerMovement() {}

    /**
     * Checks if the "walking conditions" are satisfied
     * If the user tilts the viewer (up or down) with an angle greater than THRESHOLD_ANGLE
     * it will move accordingly.
     */
    private int isWalking(float[] eulerAngles){
        pitch = Math.toDegrees(eulerAngles[0]); // Y

        if(pitch <= THRESHOLD_ANGLE * -1){
            return FORWARD;
        }

        if(pitch >= THRESHOLD_ANGLE){
            return BACKWARD;
        }

        return STILL;
    }

    /**
     * Calculates eyes's coordinates in order to achieve movement, according to direction and
     * orientation.
     */
    public float updateEyePosition (HeadTransform headTransform, float eyeZ) {
        headTransform.getForwardVector(forwardVec, 0); // (X, Y, Z)
        headTransform.getEulerAngles(eulerAngles, 0);  // (pitch, yaw, roll)

        orientation = (int)Math.signum(forwardVec[2]);
        direction = isWalking(eulerAngles);

        newEyeZ = eyeZ;

        checkBounds();

        if(direction == FORWARD && canMoveForward){
            newEyeZ = (eyeZ + nextStep()) * forwardVec[2];
            if(orientation == BEHIND){
                newEyeZ *= compensation;
            }
        }
        else if(direction == BACKWARD && canMoveBackward){
            newEyeZ = (eyeZ + nextStep()) * forwardVec[2] * -1;
            if(orientation == AHEAD){
                newEyeZ *= compensation;
            }
        }
        return newEyeZ;
    }

    private float nextStep(){
        if(direction != prevDirection){
            prevDirection = direction;
            if (orientation == AHEAD){
                if(direction == FORWARD ){
                    return step = (newEyeZ * 2 - START) * -1;
                }
                if(direction == BACKWARD){
                    return step = START;
                }
            }
            if (orientation == BEHIND){
                if(direction == FORWARD ){
                    return step = START;
                }
                if(direction == BACKWARD){
                    return step = (newEyeZ * 2 - START) * -1;
                }
            }
        }
        if(orientation != prevOrientation){
            prevOrientation = orientation;
            if(direction == FORWARD){
                if(orientation == BEHIND){
                    return step = START;
                }
                if(orientation == AHEAD){
                    return step = (newEyeZ * 2 - START) * -1;
                }
            }
            if(direction == BACKWARD){
                if(orientation == BEHIND){
                    return step = (newEyeZ * 2 - START) * -1;
                }
                if(orientation == AHEAD){
                    return step = START;
                }
            }
        }
        return step += SPEED;
    }

    private void checkBounds(){
        if ((orientation == AHEAD && newEyeZ < boundA) || (orientation == BEHIND && newEyeZ > boundB)) {
            canMoveForward = false;
        } else{
            canMoveForward = true;
        }
        if ((orientation == AHEAD && newEyeZ > boundB) || (orientation == BEHIND && newEyeZ < boundA)) {
            canMoveBackward = false;
        } else{
            canMoveBackward = true;
        }
    }
}
