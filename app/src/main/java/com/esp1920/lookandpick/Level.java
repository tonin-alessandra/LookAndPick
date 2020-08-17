package com.esp1920.lookandpick;

/**
 * This class manages game levels, intended as periods of time with specific settings.
 * We provided three different levels:
 * 1) the player has to collect as many objects as possible in a fixed amount of time;
 * 2) the player has to complete a specific task, so it has to collect only correct objects, according
 * to the level's request;
 * 3) same as point 2), but with one more difficulty: objects will disappear after a fixed amount of time,
 * so the player has to be fast to complete the request.
 */
public class Level {
    private final String TAG = "Level";

    private int levelNumber;
    private int levelDuration; // In seconds.

    // Category of the object to pick up.
    private ObjCategory mCategory;

    /**
     * Constructor.
     * A level object is initialized as the first level, so the associated number is 1 and its duration is passed as parameter.
     * In addition, the category associated is ALL, this means there are no constraints on what type of objects can be collected.
     * This values will be updated successively.
     *
     * @param duration The duration of the first level.
     */
    Level(int duration) {
        levelNumber = 1;
        setCategory(ObjCategory.ALL);
        setDuration(duration);
    }

    /**
     * @return The number of the current level.
     */
    public int getLevelNumber() {
        return levelNumber;
    }

    /**
     * Gets the category of the correct object to be collected in this level. It is useful to check if the player
     * picked up a correct target, according to the level request.
     *
     * @return An {@link ObjCategory} representing the objects' category associated with this level.
     */
    public ObjCategory getCategory() {
        return mCategory;
    }

    /**
     * Establishes which category is the correct one for this level.
     *
     * @param category The category of objects that must be collected for this level.
     */
    public void setCategory(ObjCategory category) {
        mCategory = category;
    }

    /**
     * @return The expected duration of this level.
     */
    public int getDuration() {
        return levelDuration;
    }

    /**
     * Sets the duration of the current level. This must be called when passing from a level to the subsequent.
     */
    public void setDuration(int time) {
        levelDuration = time;
    }

    /**
     * Updates the current level number in order to pass to the next one.
     * This is called when the level duration is reached.
     */
    public void nextLevel() {
        levelNumber++;
    }
}
