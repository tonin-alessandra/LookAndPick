package com.esp1920.lookandpick;

/**
 * This class represents a visible object that appears on the screen.
 * Every object is represented using its {@link Position} and an index, which indicates the associated mesh.
 * Also, each object has a {@link DisappearanceTimer}.
 */

public class PickableTarget {
    private final String TAG = "PickableTarget";

    private Position mPosition;
    private int mMeshIndex;
    private DisappearanceTimer mTimer;

    /**
     * Constructor. It does not initialize the mesh index.
     */
    PickableTarget() {
        mPosition = new Position();
        mPosition.generateRandomPosition();
        mTimer = new DisappearanceTimer();
    }

    /**
     * Constructor.
     *
     * @param index Initial index of the mesh.
     */
    PickableTarget(int index) {
        mPosition = new Position();
        mPosition.generateRandomPosition();
        setMeshIndex(index);
        mTimer = new DisappearanceTimer();

    }

    /**
     * Changes the position with a random new one.
     */
    public void randomPosition() {
        mPosition.generateRandomPosition();
    }

    /**
     * Gets the position of the {@link PickableTarget} object.
     *
     * @return The {@link Position} object.
     */
    public Position getPosition() {
        return mPosition;
    }

    /**
     * Sets the position.
     *
     * @param position A new Position.
     */
    public void setPosition(Position position) {
        mPosition = position;
    }

    /**
     * Gets the index of the current mesh.
     *
     * @return the current value of the index.
     */
    public int getMeshIndex() {
        return mMeshIndex;
    }

    /**
     * Sets a new index.
     *
     * @param meshIndex
     */
    public void setMeshIndex(int meshIndex) {
        mMeshIndex = meshIndex;
    }

    /**
     * Gets the timer.
     *
     * @return A {@link DisappearanceTimer} object.
     */
    public DisappearanceTimer getTimer() {
        return mTimer;
    }

    /**
     * Checks if an object must be hidden.
     */
    public boolean isHidden() {
        return mTimer.timeFinished();
    }
}
