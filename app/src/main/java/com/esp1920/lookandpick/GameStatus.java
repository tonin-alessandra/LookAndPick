package com.esp1920.lookandpick;

/**
 * This class represents a generic status of the game.
 * It is used to represent the lives counter or the score counters.
 */
//TODO: nome provvisorio per ora
public class GameStatus {
    private final String TAG = "GameStatus";
    private int counter;
    private StatusManager mScoreManager;

    /**
     * Constructor.
     *
     * @param initialValue The initial value of the counter.
     */
    GameStatus(int initialValue) {
        counter = initialValue;
    }

    /**
     * Increases the counter of a given value.
     *
     * @param amount
     */
    public void increase(int amount) {
        counter += amount;
    }

    /**
     * Decreases the counter of a given value.
     *
     * @param amount
     */
    public void decrease(int amount) {
        counter -= amount;
    }

    /**
     * Gets the counter value.
     *
     * @return The counter value.
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Checks if there is game over.
     *
     * @return True if the counter reached zero, false otherwise.
     */
    public boolean gameOver() {
        if (counter == 0)
            return true;
        return false;
    }

    /**
     * Saves the counter value with a given TAG.
     *
     * @param score The value to save.
     */
    public void saveValue(int score) {
        mScoreManager.saveScore(score);
    }
}
