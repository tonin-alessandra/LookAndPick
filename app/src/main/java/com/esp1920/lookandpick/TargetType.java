package com.esp1920.lookandpick;

import java.util.Random;

/**
 * This enum defines the categories to which an object can belong.
 */
enum ObjCategory {
    ANIMAL(R.string.animals),
    PLANT(R.string.plants),
    ANDROID_BOT(R.string.android_bot),
    // This can be useful to identify particular or special objects, such as Pikachu.
    BONUS(R.string.bonus),
    // This is used for the first level, where there are no constraints on what type of objects can be collected.
    ALL(R.string.all),
    ROOM(R.string.room);

    private int category;
    // Used to avoid selecting ALL or ROOM as a random category for the levels.
    private final static int LAST_TWO = 2;

    /**
     * Constructor.
     *
     * @param description The description associated to each category. Useful when creating levels' requests.
     */
    ObjCategory(int description) {
        this.category = description;
    }

    /**
     * @return The description associated to this category.
     */
    public int getDescription() {
        return category;
    }

    /**
     * Picks a random value of {@code ObjCategory}.
     * Needed to create levels' requests.
     *
     * @return The selected random value.
     */
    public static ObjCategory getRandomCategory() {
        Random random = new Random();
        return values()[random.nextInt(values().length - LAST_TWO)];
    }
}

/**
 * This enum defines the possible names of an object.
 */
enum ObjName {
    PENGUIN,
    CAT,
    MOUSE,
    SUNFLOWER,
    CACTUS,
    GREEN_ANDROID,
    PLANE,
    PIKACHU,
    ROOM;
}
