package com.esp1920.lookandpick;

import com.google.vr.sdk.base.HeadTransform;


/**
 * Class to handle user movement along the Z axis.
 *
 * If the user tilts the cardboard viewer DOWN with an angle greater than the threshold, the player
 * will start moving forward, along the Z axis, depending on the direction the head is looking at.
 * If the user tilts the cardboard viewer UP with an angle greater than the threshold, the player
 * will move backwards.
 * Movement is enabled only along the Z axis. The player will move only with respect to the walls
 * that are in front of and behind him/her when the game starts (wall with pink tiles and wall with
 * blue tiles).
 *
 * Movement can be performed in two ways:
 *      1) tilting the viewer up or down and keeping it tilted for some seconds.
 *         This will result in a fluid movement with direction and orientation according to what has
 *         been said previously.
 *      2) tilting the viewer up or down and then quickly returning to the "standard" position.
 *         This will result in the player taking single steps.
 *
 * This feature is designed to be comfortable when using a Cardboard viewer: boundaries to movement
 * forward and backward are added for this reason.
 */
public class PlayerMovement {
    private final String TAG = "PlayerMovement";

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
    // along the Z axis.
    // The wall AHEAD is the pink wall (ahead when game begins)
    // The wall BEHIND is the blue wall (behind when game begins)
    private static final int AHEAD = -1;
    private static final int BEHIND = 1;
    private int prevOrientation = 0; // prevOrientation is set to an invalid orientation value
    private int orientation;

    private float[] eulerAngles = new float[3];
    private float[] forwardVec = new float[3];

    // rotation around the X axis in the head's reference system
    private double pitch;

    private float newEyeZ;

    private static final float START = 0.25f; // first step
    private static final float SPEED = 0.03f; // movement speed
    private float step;

    private float compensation = 0.65f;

    private boolean canMoveForward;
    private boolean canMoveBackward;

    // Movement boundaries to avoid discomfort and to ensure a fluid movement.
    // The user cannot walk past these points.
    private static final float boundA = -0.9f; // bound regarding the wall Ahead
    private static final float boundB = 5.5f;   // bound regarding the wall Behind

    /**
     * Constructor.
     */
    public PlayerMovement() {
    }

    /**
     * Checks if and how the player is moving the viewer, to move in the room.
     * If the user tilts the viewer (up or down) with an angle greater than THRESHOLD_ANGLE,
     * he/she will begin to move accordingly.
     *
     * @param eulerAngles vector of Euler angles (pitch, yaw, roll) that describe head's movement
     *                    around the X, Y and Z axes respectively (head's reference system).
     * @return direction  in which the user wants to walk.
     */
    private int getDirection(float[] eulerAngles) {
        // Gets pitch, rotation of the head around the X axis, in order to detect viewer-tilting.
        pitch = Math.toDegrees(eulerAngles[0]);

        // If viewer is tilted down with angle greater than the threshold, the user will move
        // forward, towards the wall he/she is facing.
        if (pitch <= THRESHOLD_ANGLE * -1) {
            return FORWARD;
        }

        // If viewer is tilted up with angle greater than the threshold, the user will move
        // backward, towards the wall he/she is facing.
        if (pitch >= THRESHOLD_ANGLE) {
            return BACKWARD;
        }

        // The user will not move.
        return STILL;
    }

    /**
     * Calculates the position of the eyes along the Z axis, according to direction and
     * orientation.
     *
     * @param headTransform Object that describes the head rotation.
     * @param eyeZ          Position of the eyes along the Z axis that has to be updated in case
     *                      of movement.
     * @return newEyeZ      The updated position of the eyes along the Z axis.
     *
     * Update of the eye position along the Z axis.
     * Note that this axis is the Z axis in the X-Y-Z right-handed coordinate system. This means that
     * it crosses the room from the wall AHEAD to the wall BEHIND, so it points to the back
     * of the room. For this reason, if we want to move towards the wall AHEAD (forward or backward),
     * the eye position has to DECREMENT. If we want to move towards the wall BEHIND, the eye position
     * has to INCREMENT.
     * It is also fundamental to keep in mind that the Z component of the forward vector
     * (direction the head is looking towards as a 3x1 vector) is positive when facing BEHIND
     * and negative when facing AHEAD.
     *
     * ---> 1) when moving TOWARDS the wall AHEAD:
     *
     *      ---> 1a) facing ahead and moving forward
     *           As said before, we have that the Z component of the forward vector (from now on,
     *           called "forwardVec[2]") is negative and the eye position has to decrement.
     *
     *           new eye position = (old eye position + step) * forwardVec[2]
     *
     *           To the old eye position, we add the step and then we multiply for forwardVec[2].
     *           This multiplication ensures that the orientation of the head remains the same while
     *           the user moves. Since forwardVec[2] is negative and the new eye position has to be
     *           less than the previous one, (old eye position + step) is a quantity that increments.
     *           Further details concerning this will be given in the method nextStep().
     *
     *      ---> 1b) facing behind and moving backward
     *                In this case, forwardVec[2] is positive and the eye position has to decrement.
     *
     *           new eye position = (old eye position + step) * -1 * forwardVec[2]
     *
     *           Again, (old eye position + step) is a quantity that increments. To make the eye
     *           position decrement as it should, we multiply everything for -1, since forwardVec[2]
     *           is positive.
     *
     * ---> 2) when moving TOWARDS the wall BEHIND:
     *
     *      ---> 2a) facing behind and moving forward
     *           Here we have a positive forwardVec[2] and the eye position has to increment.
     *
     *           new eye position = (old eye position + step) * forwardVec[2] * compensation
     *
     *           The sum (old eye position + step) increments as the player moves, so the eye
     *           position calculated increments correctly.
     *           When the user is facing behind and tilts the viewer down to move forward, forwardVec[2]
     *           has a value grater than the value of forwardVec[2] in the previous cases. This would
     *           result in a faster movement. To compensate this effect, the new eye position is
     *           multiplied for a compensation value. This value (0.65) has been chosen empirically.
     *
     *      ---> 2b) facing ahead and moving backward
     *           In this case, forwardVec[2] is negative and the eye position has to increment.
     *
     *           new eye position = (old eye position + step) * forwardVec[2] * -1 * compensation
     *
     *           This formulas follows the previous cases: (old eye position + step) increments and
     *           we multiply for -1 since forwardVec[2] is negative. We need to multiply for
     *           compensation, for the same reason explained in case 2a.
     *
     */
    public float updateEyePosition(HeadTransform headTransform, float eyeZ) {
        // Gets head's forward vector to get orientation.
        headTransform.getForwardVector(forwardVec, 0); // (X, Y, Z)

        // Gets Euler angles to get head's rotation around X, Y and Z axes respectively.
        headTransform.getEulerAngles(eulerAngles, 0);  // (pitch, yaw, roll)

        // Gets the sign of the Z component of the forward vector to identify orientation.
        orientation = (int) Math.signum(forwardVec[2]);

        direction = getDirection(eulerAngles);

        newEyeZ = eyeZ;

        // Player does not move
        if (direction == STILL) {
            return newEyeZ;
        }

        // Checks if the player can walk or must stop, depending on the movement boundaries.
        checkBounds();

        // Update the eye position along the Z axis.
        if (direction == FORWARD && canMoveForward) {
            newEyeZ = (eyeZ + nextStep()) * forwardVec[2]; // Updates eye position
            if (orientation == BEHIND) {
                newEyeZ *= compensation;
            }
        } else if (direction == BACKWARD && canMoveBackward) {
            newEyeZ = (eyeZ + nextStep()) * -1 * forwardVec[2]; // Updates eye position
            if (orientation == AHEAD) {
                newEyeZ *= compensation;
            }
        }
        return newEyeZ;
    }

    /**
     * Calculates the next step, in order to make the player move.
     *
     * @return the value of the next step.
     *
     * Computation of the next step.
     * As soon as the user tilts the viewer, the direction changes (STILL --> FORWARD/BACKWARD)
     * and it remains the same until movement stops.
     * The player starts moving making a "first step" and then the movement goes on with a certain speed.
     * This means that the value of the step increases as movement goes on. When the player decides
     * to move in a different direction or orientation, the step has to be reset to its initial value.
     * Note that when the player moves taking single steps one after the other, direction changes
     * (STILL ---> FORWARD/BACKWARD ---> STILL). Of course taking an "instantaneous step" is impossible.
     * The check on the pitch to get the movement direction is done very frequently and the pitch
     * remains in the "movement interval" for some time as the player moves the viewer.
     *
     * When direction and orientation remain the same, next step = step + speed (trivial).
     * When direction or orientation change, next step = first step.
     * The value of first step depends on how we are moving: as explained before, (old eye position + step)
     * is a quantity that has to increment and we have to keep in mind that old eye position isn't
     * always positive.
     * Computing of the first step.
     *
     * ---> 1) moving forward (new eye position = (old eye position + step) * forwardVec[2]).
     *
     *      ---> 1a) the user is facing behind, forwardVec[2] is positive.
     *
     *           first step = START
     *
     *           START is 0.25 and it is the base value of the step. If we add START to the old
     *           eye position everything works because (old eye position + step) increments and
     *           forwardVec[2] is positive, so the new eye position increments as it should, and
     *           the player takes one step forward.
     *
     *      ---> 1b) the user is facing ahead, forwardVec[2] is negative.
     *
     *           Here we are moving towards the wall AHEAD, so the eye position has to decrement.
     *           As new eye position, we want something like this: old eye position - START.
     *           Considering that forwardVec[2] is negative, we want:
     *
     *           old eye position + step = START - old eye position
     *
     *           so it must be: first step = (old eye position * 2 - START) * -1
     *
     * ---> 2) moving backward (new eye position = (old eye position + step) * -1 * forwardVec[2]).
     *
     *      ---> 2a) the user is facing behind, forwardVec[2] is positive.
     *
     *           We are moving towards the wall AHEAD, so the the eye position has to decrement.
     *           The reasoning for this case is the same seen for case 1b. In fact, forwardVec[2] is
     *           positive, but we multiply it for -1. We want:
     *
     *           old eye position + step = START - old eye position
     *
     *           so it must be: first step = (old eye position * 2 - START) * -1
     *
     *      ---> 2b) the user is facing ahead, forwardVec[2] is negative.
     *
     *           We are moving towards the wall BEHIND, so the eye position has to increment.
     *           forwardVec[2] is negative but we multiply it for -1, so:
     *
     *           first step = START
     *
     * We can see that
     *
     * first step = (old eye position * 2 - START) * -1
     *
     * whenever we move towards the wall AHEAD, forward or backward, while
     *
     * first step = START
     *
     * when moving towards the wall BEHIND.
     *
     */
    private float nextStep() {

        // Resets step when needed
        if ((direction != prevDirection) || (orientation != prevOrientation)) {
            prevDirection = direction;
            prevOrientation = orientation;
            // moving towards the wall AHEAD
            if ((orientation == AHEAD && direction == FORWARD) || (orientation == BEHIND && direction == BACKWARD)) {
                return step = (newEyeZ * 2 - START) * -1;
            }
            // moving towards the wall BEHIND
            if ((orientation == AHEAD && direction == BACKWARD) || (orientation == BEHIND && direction == FORWARD)) {
                return step = START;
            }
        }

        return step += SPEED;
    }

    /**
     * Check if the user can move forward and backward, according to the boundaries set.
     *
     * boundA is the bound regarding the wall AHEAD; boundB regards the wall BEHIND.
     * To understand conditions, keep in mind that moving towards the wall AHEAD means moving
     * towards the negative values of the Z axis.
     */
    private void checkBounds() {
        if ((orientation == AHEAD && newEyeZ < boundA) || (orientation == BEHIND && newEyeZ > boundB)) {
            canMoveForward = false;
        } else {
            canMoveForward = true;
        }
        if ((orientation == AHEAD && newEyeZ > boundB) || (orientation == BEHIND && newEyeZ < boundA)) {
            canMoveBackward = false;
        } else {
            canMoveBackward = true;
        }
    }
}
