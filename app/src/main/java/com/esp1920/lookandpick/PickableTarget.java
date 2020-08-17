package com.esp1920.lookandpick;

import androidx.annotation.Nullable;

/**
 * This class represents a visible object that appears on the screen.
 * Every object is represented using its {@link Position} and an index, which indicates the associated mesh.
 * Also, each object has a {@link DisappearanceTimer}, which is a timer used to make the object
 * disappear from the scene.
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
     * Changes both index and target with new ones.
     *
     * @param newMeshIndex The new index.
     * @param newTarget    The new {@link Target} object with its mesh index.
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

    /**
     * @return the current {@link Position} of the object.
     */
    public Position getPosition() {
        return mPosition;
    }

    /**
     * Sets the position.
     *
     * @param position The new position.
     */
    public void setPosition(Position position) {
        mPosition = position;
    }

    /**
     * @return the current value of the index.
     */
    public int getMeshIndex() {
        return mMeshIndex;
    }

    /**
     * Sets a new index.
     *
     * @param meshIndex The new mesh index.
     */
    public void setMeshIndex(int meshIndex) {
        mMeshIndex = meshIndex;
    }

    /**
     * @return The {@link Target} object associated to this.
     */
    public Target getTarget() {
        return mTarget;
    }

    /**
     * Sets a new Target to the current object.
     *
     * @param target The {@link Target} object to associate.
     */
    public void setTarget(Target target) {
        mTarget = target;
    }

    /**
     * @return The {@link DisappearanceTimer} object.
     */
    public DisappearanceTimer getTimer() {
        return mTimer;
    }
}
