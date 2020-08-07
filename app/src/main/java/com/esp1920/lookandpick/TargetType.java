package com.esp1920.lookandpick;

import java.util.Random;

/**
 * This enum defines the categories to which an object can belong.
 */
enum ObjCategory {
    ANIMAL("animals"),
    PLANT("plants"),
    ANDROID_BOT("android bot"),
    //This can be useful to identify particular or special objects, such as Pikachu.
    BONUS("bonus"),
    //This is used for the first level, where there are no constraints on what type of objects can be collected
    ALL("what you want"),
    ROOM("room");

    private String category;

    ObjCategory(String description) {
        this.category = description;
    }

    public String getDescription() {
        return category;
    }

    /**
     * Picks a random value of {@code ObjCategory}.
     *
     * @return The selected random value.
     */
    public static ObjCategory getRandomCategory() {
        Random random = new Random();
        return values()[random.nextInt(values().length - 1)];
    }
}

/**
 * This enum defines the name of an object.
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

    /**
     * Picks a random value of {@code ObjName}.
     *
     * @return The selected random value.
     */
    public static ObjName getRandomName() {
        Random random = new Random();
        return values()[random.nextInt(values().length - 1)];
    }
}
