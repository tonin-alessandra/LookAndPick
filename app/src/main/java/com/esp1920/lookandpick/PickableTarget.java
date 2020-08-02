package com.esp1920.lookandpick;

/**
 * This class represents a visible object that appears on the screen.
 * Every object is represented using its position and an index, which indicates the associated mesh.
 */

public class PickableTarget {
    private Position mPosition;
    private int mMeshIndex;
    private DisappearanceTimer mTimer;

    /**
     * Constructor.
     */
    PickableTarget(){
        mPosition = new Position();
        mPosition.generateRandomPosition();
        mTimer = new DisappearanceTimer();
    }

    /**
     * Constructor.
     * @param index Initial index of the mesh.
     */
    PickableTarget(int index){
        mPosition = new Position();
        mPosition.generateRandomPosition();
        setMeshIndex(index);
    }

    /**
     * Changes the position with a random new one.
     */
    public void randomPosition(){
        mPosition.generateRandomPosition();
    }

    /**
     * Gets the position.
     * @return A {@link Position} object.
     */
    public Position getPosition() {
        return mPosition;
    }

    /**
     * Gets the index of the current mesh.
     * @return the current value of the index.
     */
    public int getMeshIndex() {
        return mMeshIndex;
    }

    /**
     * Sets a new index.
     * @param meshIndex
     */
    public void setMeshIndex(int meshIndex) {
        mMeshIndex = meshIndex;
    }

    /**
     * Gets the timer.
     * @return A {@link DisappearanceTimer} object.
     */
    public DisappearanceTimer getTimer() {
        return mTimer;
    }

    /**
     * Checks if an object must be hidden.
     */
    public boolean isHidden(){
        return mTimer.timeFinished();
    }
}
