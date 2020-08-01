package com.esp1920.lookandpick;

public class PickableTarget {
    private Position mPosition;
    private int mMeshIndex;

    /**
     * Constructor.
     */
    PickableTarget(){
        mPosition = new Position();
        mPosition.generateRandomPosition();
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
     * @return a Position object.
     */
    public Position getPosition() {
        return mPosition;
    }

    /**
     * Sets the Position.
     * @param position A Position Object.
     */
    public void setPosition(Position position) {
        mPosition = position;
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
}
