package com.esp1920.lookandpick;

import com.google.vr.sdk.base.HeadTransform;


/**
 * Class to handle user movement along Z axis.
 *
 * If the user tilts the cardboard viewer down with an angle greater than the threshold, the player
 * will start moving forward in the direction the head is looking at.
 * If the user tilts the cardboard viewer up with an angle greater than the threshold, the player
 * will move backwards.
 * Movement is enabled only along the Z axis. If the user looks at the wall to the left and tilts the
 * viewer, for example, he/she won't move.
 *
 * This feature is designed to be comfortable when using a Cardboard viewer.
 *
 * Explanations of the formulas used in the code below are provided in the following comments.
 * Please note that we came up with these formulas by ourselves, after a lot of tests and experiments.
 * These were not found on the internet, in fact, examples of movement in virtual reality, using
 * Google VR SDK for Android Studio, cannot be found anywhere.
 *
 */
 public class PlayerMovement {

    private static final double THRESHOLD_ANGLE = 30; // degrees

    // Direction represents the *type* of movement: this can be FORWARD or BACKWARD, depending on
    // how the viewer is tilted.
    private static final int FORWARD = 1;
    private static final int BACKWARD = -1;
    private static final int STILL = 0; // player does not move
    private int prevDirection = -2; // prevDirection is set to an invalid direction value
    private int direction;

    // Orientation represents the wall the user is facing and looking at.
    // The only 'valid' walls are the wall AHEAD and the wall BEHIND, as movement is performed
    // along the z axis.
    // The wall AHEAD is the purple wall (ahead when game begins)
    // The wall BEHIND is the blue wall (behind when game begins)
    private static final int AHEAD = -1;
    private static final int BEHIND = 1;
    private int prevOrientation = 0; // prevOrientation is set to an invalid orientation value
    private int orientation;

    private float[] eulerAngles = new float[3];
    private float[] forwardVec = new float[3];

    private double pitch;

    private float newEyeZ;

    private static final float START = 0.25f;
    private static final float SPEED = 0.03f;
    private float step;

    private float compensation = 0.65f;

    private boolean canMoveForward;
    private boolean canMoveBackward;

    // Movement boundaries to avoid discomfort and to ensure a fluid movement.
    // The user cannot walk past these points.
    private static  final float boundA = -0.9f; // bound regarding the wall Ahead
    private static final float boundB = 5.5f;   // bound regarding the wall Behind

    public PlayerMovement() {}

    /**
     * Checks if and how the player is moving the viewer to walk.
     * If the user tilts the viewer (up or down) with an angle greater than THRESHOLD_ANGLE,
     * he/she will begin to move accordingly.
     *
     * @param eulerAngles vector of Euler angles (pitch, yaw, roll) that describe head's movement
     *                    around the Y, Z and X axis respectively.
     * @return direction in which the user wants to walk.
     */
    private int getDirection(float[] eulerAngles){
        // Gets pitch, rotation of the head around the Y axis, in order to detect viewer-tilting.
        // Note that this axis is different from the Y axis of the room's reference system.
        pitch = Math.toDegrees(eulerAngles[0]);

        // Condition: if viewer is tilted down with angle greater than the threshold
        if(pitch <= THRESHOLD_ANGLE * -1){
            return FORWARD;
        }

        // Condition: if viewer is tilted up with angle greater than the threshold
        if(pitch >= THRESHOLD_ANGLE){
            return BACKWARD;
        }

        return STILL;
    }

    /**
     * Calculates the position of the eyes along the z axis, according to direction and
     * orientation.
     *
     * @param headTransform object that describes the head rotation
     * @param eyeZ position of the eyes along the z axis that has to be updated in case
     *             of movement
     * @return the updated position of the eyes along the z axis.
     *
     * Update of the eye position along the Z axis.
     * Note that this axis is the Z axis in the room's reference system, which is right-hand  based.
     * The axis crosses the room from the wall AHEAD to the wall BEHIND, so it points to the back
     * of the room. For this reason, if we want to move towards the wall AHEAD (forward or backward),
     * the eye position has to DECREMENT. If we want to move towards the wall BEHIND, the eye position
     * has to INCREMENT.
     *
     * ---> when moving FORWARD:
     *
     */
    public float updateEyePosition (HeadTransform headTransform, float eyeZ) {
        // Gets head's forward vector to get orientation
        headTransform.getForwardVector(forwardVec, 0); // (X, Y, Z)

        // Gets Euler angles to get head's rotation around Y, Z and X axis respectively
        headTransform.getEulerAngles(eulerAngles, 0);  // (pitch, yaw, roll)

        // Gets the sign of the z component of the forward vector to identify orientation
        orientation = (int)Math.signum(forwardVec[2]);

        direction = getDirection(eulerAngles);

        newEyeZ = eyeZ;

        // Player does not move
        if(direction == STILL){
            return newEyeZ;
        }

        // Checks if the player can walk or must stop
        checkBounds();

        if(direction == FORWARD && canMoveForward){
            newEyeZ = (eyeZ + nextStep()) * forwardVec[2]; // Updates eye position
            if(orientation == BEHIND){
                newEyeZ *= compensation;
            }
        }
        else if(direction == BACKWARD && canMoveBackward){
            newEyeZ = (eyeZ + nextStep()) * forwardVec[2] * -1; // Updates eye position
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
