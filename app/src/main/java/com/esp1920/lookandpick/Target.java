package com.esp1920.lookandpick;

import static com.esp1920.lookandpick.ObjCategory.*;

/**
 * This class represents a generic target object that will be rendered to appear on the scene.
 * All operations to manage a Target behaviour can be found in {@link TargetManager}.
 */
public class Target {
    private final String TAG = "Target";

    private ObjName mName;
    private ObjCategory mCategory;
    private String mFilePath;
    private String mSelectedTexturePath;
    private String mNotSelectedTexturePath;

    private int mScore = 1;

    /**
     * Constructor.
     *
     * @param name               The name of the target object. Must be selected from {@link ObjName}.
     * @param filePath           The path to the obj model for this target object.
     * @param selectedTexture    The path to the png selected texture for this target object.
     * @param notSelectedTexture The path to the png not selected texture for this target object.
     */
    public Target(ObjName name, String filePath, String selectedTexture, String notSelectedTexture) {
        setName(name);
        setCategory();
        setFilePath(filePath);
        setSelectedTexturePath(selectedTexture);
        setNotSelectedTexturePath(notSelectedTexture);
    }

    /**
     * Gets the name of this target object.
     *
     * @return The name as an {@link ObjName} object.
     */
    public ObjName getName() {
        return mName;
    }

    /**
     * Sets up the name of this object. Must be selected from {@link ObjName}.
     *
     * @param name The name of the target object.
     */
    private void setName(ObjName name) {
        this.mName = name;
    }

    /**
     * Gets the category of this target object.
     *
     * @return The category as an {@link ObjCategory} object.
     */
    public ObjCategory getCategory() {
        return mCategory;
    }

    /**
     * Sets up the category of this object, according to his name.
     * Must be selected from {@link ObjCategory}.
     */
    private void setCategory() {
        ObjName objName = getName();
        switch (objName) {
            case CACTUS:
            case SUNFLOWER:
                mCategory = PLANT;
                break;
            case CAT:
            case PENGUIN:
            case MOUSE:
                mCategory = ANIMAL;
                break;
            case GREEN_ANDROID:
                mCategory = ANDROID_BOT;
                break;
            case PIKACHU:
            case PLANE:
                mCategory = BONUS;
                break;
            case ROOM:
                mCategory = ObjCategory.ROOM;
                break;
        }
    }


    /**
     * @return Returns the score associated to this object.
     */
    public int getScore() {
        return mScore;
    }

    /**
     * Gets the texture png file associated to this object when it is not selected by the user.
     *
     * @return The image to be applied as texture.
     */
    public String getNotSelectedTexturePath() {
        return mNotSelectedTexturePath;
    }

    /**
     * Sets the path to the file to be used as a texture when the object is not selected by the user.
     *
     * @param notSelectedTexturePath The path to the png file.
     */
    private void setNotSelectedTexturePath(String notSelectedTexturePath) {
        mNotSelectedTexturePath = notSelectedTexturePath;
    }

    /**
     * Gets the texture png file associated to this object when it is selected by the user.
     *
     * @return The image to be applied as texture.
     */
    public String getSelectedTexturePath() {
        return mSelectedTexturePath;
    }

    /**
     * Sets the path to the file to be used as a texture when the object is selected by the user.
     *
     * @param selectedTexturePath The path to the png file.
     */
    private void setSelectedTexturePath(String selectedTexturePath) {
        mSelectedTexturePath = selectedTexturePath;
    }

    /**
     * Gets the path to the .obj model file used to create this target object.
     *
     * @return The .obj file.
     */
    public String getFilePath() {
        return mFilePath;
    }

    /**
     * Sets the path to the .obj model file used to create this target object.
     *
     * @param filePath The path to the .obj file.
     */
    private void setFilePath(String filePath) {
        this.mFilePath = filePath;
    }

    /**
     * @return The tag which identifies an object of this class.
     */
    public String getTAG() {
        return TAG;
    }

}
