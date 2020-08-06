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
    private Target mTarget;

    /**
     * Constructor. It does not initialize the mesh index.
     */
    PickableTarget() {
        mPosition = new Position();
        mPosition.generateRandomPosition();
        mTimer = new DisappearanceTimer();
    }

    /**
     * Constructor. It does not initialize the mesh index.
     *
     * @param duration Initial timer duration expressed in seconds.
     */
    PickableTarget(int duration) {
        mPosition = new Position();
        mPosition.generateRandomPosition();
        mTimer = new DisappearanceTimer(duration * 1000);
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
     * Gets the {@link Target} instance.
     * @return The Target object.
     */
    public Target getTarget() {
        return mTarget;
    }

    /**
     * Sets the {@link Target} associated to the PickableTarget objects.
     * @param target The target to associate.
     */
    public void setTarget(Target target) {
        mTarget = target;
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
     * Changes the timer duration with a different one.
     *
     * @param duration The duration of the new timer in seconds.
     */
    public void newTimer(long duration) {
        if (mTimer != null)
            mTimer.stopTimer();
        mTimer = new DisappearanceTimer(duration*1000);
    }

    /**
     * Changes the timer duration with the default one defined in {@link DisappearanceTimer}.
     */
    public void defaultTimer() {
        if (mTimer != null)
            mTimer.stopTimer();
        mTimer = new DisappearanceTimer();
    }

    /**
     * Checks if an object must be hidden.
     */
    public boolean isHidden() {
        return mTimer.timeFinished();
    }
}
