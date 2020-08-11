package com.esp1920.lookandpick;

import androidx.annotation.Nullable;

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
     * Constructor. It does not initialize the mesh index and the timer.
     */
    PickableTarget() {
        mPosition = new Position();
        mPosition.generateRandomPosition();
    }

    /**
     * Initializes a timer for this object.
     * It has been separated from the constructor because not always the timer is needed, so it
     * is created only when necessary.
     */
    public void initializeTimer(int duration) {
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
     *
     * @return The Target object.
     */
    public Target getTarget() {
        return mTarget;
    }

    /**
     * Sets the {@link Target} associated to the PickableTarget objects.
     *
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
    @Nullable
    public DisappearanceTimer getTimer() {
        return mTimer;
    }

    /**
     * Changes both mMeshIndex and mTarget with new ones.
     *
     * @param newMeshIndex The new index.
     * @param newTarget    The new target associated with the index.
     */
    public void changeMesh(int newMeshIndex, Target newTarget) {
        setMeshIndex(newMeshIndex);
        setTarget(newTarget);
    }

    /**
     * Checks if an object must be hidden.
     */
    public boolean isHidden() {
        if (mTimer != null) return mTimer.timeFinished();
        return false;
    }
}
