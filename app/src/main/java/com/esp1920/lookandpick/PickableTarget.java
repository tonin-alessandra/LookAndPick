package com.esp1920.lookandpick;

/**
 * This class represents a visible object that appears on the screen.
 * Every object is represented using its {@link Position} and an index, which indicates the associated mesh.
 * Also, each object has a {@link DisappearanceTimer}, which is a timer used to make the object
 * disappear from the scene. Notice that the timer is initialized only if the current game's level is number 3.
 */

public class PickableTarget {
    private final String TAG = "PickableTarget";
    // Used to convert seconds to millis.
    private final static int TO_MILLIS = 1000;

    private Position mPosition;
    private int mMeshIndex;
    private DisappearanceTimer mTimer;
    private Target mTarget;

    /**
     * Constructor. It does not initialize the index and the timer.
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
        mTimer = new DisappearanceTimer(duration * TO_MILLIS);
    }

    /**
     * Changes both the mesh index and the target associated with this object.
     *
     * @param newMeshIndex The index associated to the new mesh.
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
     * @return The current {@link Position} of this object.
     */
    public Position getPosition() {
        return mPosition;
    }

    /**
     * @return The current {@link Position} of this object as an array.
     */
    public float[] getPositionAsArray() {
        return new float[]{mPosition.getXCoordinate(), mPosition.getYCoordinate(),
                mPosition.getZCoordinate()};
    }

    /**
     * Sets the position of this object.
     *
     * @param position The position where the object will appear.
     */
    public void setPosition(Position position) {
        mPosition = position;
    }

    /**
     * @return The current value of the index.
     */
    public int getMeshIndex() {
        return mMeshIndex;
    }

    /**
     * Sets a new index, which represents the mesh associated with this object.
     *
     * @param meshIndex The new index.
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
     * Associates a new {@link Target} to the current object.
     *
     * @param target The {@link Target} object to link.
     */
    public void setTarget(Target target) {
        mTarget = target;
    }

    /**
     * @return The {@link DisappearanceTimer} object associated to this PickableTarget.
     */
    public DisappearanceTimer getTimer() {
        return mTimer;
    }
}
