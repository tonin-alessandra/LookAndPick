package com.esp1920.lookandpick;

/**
 * Class to handle user movement in the scene
 */
public class Movement {

    //angle between 0 and 90 degrees, if the user tilts the cardboard viewer down with
    //an angle greater than this threshold angle, the player will start moving
    //in the direction the head is looking at
    private final float THRESHOLD_ANGLE = 25;

    private boolean isWalking = false;

    //walking speed
    private float speed;

    private Movement() {}

    /**
     * Checks if the "walking conditions" are satisfied
     * TODO: write a better comment
     */
    public void isWalking(){

    }

    /**
     * Makes user walk forward -> makes the scene move backwards
     * TODO: write a better comment
     */
    public void walk(){

    }
}
