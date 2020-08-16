package com.esp1920.lookandpick;

import android.content.Context;

/**
 * This class handles a generic status of the game, made up by the actual player score and lives.
 */
public class GameStatus {
    private final String TAG = "GameStatus";

    private StatusManager mScoreManager;

    private int score;
    private int lives;

    /**
     * Constructor.
     *
     * @param initialScore The initial value of the score. At the beginning of the game, it should be zero.
     * @param initialLives The initial value of the player's lives.
     * @param context      The current application context.
     */
    GameStatus(int initialScore, int initialLives, Context context) {
        score = initialScore;
        lives = initialLives;
        mScoreManager = StatusManager.getInstance(context);
    }

    /**
     * Increases the score by the specified amount of points.
     * This is called when the player collects a correct {@link PickableTarget}.
     *
     * @param points The number of points to add to the total score.
     */
    public void increaseScore(int points) {
        score += points;
    }

    /**
     * Decreases the number of lives by the specified amount.
     * This is called when the player collects a wrong {@link PickableTarget}.
     *
     * @param amount The number of lives to remove.
     */
    public void decreaseLives(int amount) {
        lives -= amount;
    }

    /**
     * @return The actual score value.
     */
    public int getScore() {
        return score;
    }

    /**
     * @return The actual number of lives.
     */
    public int getLives() {
        return lives;
    }

    /**
     * Checks if there is game over.
     *
     * @return True if the lives' number reached zero, false otherwise.
     */
    public boolean isGameOver() {
        if (lives == 0) {
            return true;
        }
        return false;
    }

    /**
     * Saves the actual score to sharedPreferences.
     */
    public void saveCurrentScore() {
        mScoreManager.saveScore(score);
    }
}
