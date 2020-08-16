package com.esp1920.lookandpick;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * This class provides all necessary methods to manage a {@link Target} object behaviour.
 * It is managed as a Singleton because only one manager can exist to avoid collisions on target's related operations.
 */
public class TargetManager {
    private final String TAG = "TargetManager";

    private static TargetManager instance;

    private TexturedMesh mTexturedMesh;
    private Texture mNotSelectedTexture;
    private Texture mSelectedTexture;

    /**
     * Constructor. It is private due to Singleton.
     */
    private TargetManager() {
    }

    /**
     * Manages TargetManager object according to Singleton design pattern.
     *
     * @returns A new instance of TargetManager or the current one, if it exists.
     */
    synchronized public static TargetManager getInstance() {
        if (instance == null)
            instance = new TargetManager();
        return instance;
    }

    /**
     * Applies the textures of a generic target object.
     *
     * @param context             The current application context.
     * @param object              The target object to which apply textures.
     * @param objectPositionParam The position attribute in the shader.
     * @param objectUvParam       The UV attribute in the shader.
     */
    public void applyTexture(Context context, Target object, int objectPositionParam, int objectUvParam) {
        try {
            mTexturedMesh = new TexturedMesh(context, object.getFilePath(), objectPositionParam, objectUvParam);
            mSelectedTexture = new Texture(context, object.getSelectedTexturePath());
            mNotSelectedTexture = new Texture(context, object.getNotSelectedTexturePath());
        } catch (IOException e) {
            Log.e(TAG, "Unable to initialize objects", e);
        }
    }

    /**
     * Gets the rendered target object.
     *
     * @return A {@link TexturedMesh} object.
     */
    public TexturedMesh getTexturedMesh() {
        return mTexturedMesh;
    }

    /**
     * Gets the texture used for the target when it is selected.
     *
     * @return A {@link Texture} object.
     */
    public Texture getSelectedTexture() {
        return mSelectedTexture;
    }

    /**
     * Gets the texture used for the target when it is not selected.
     *
     * @return A {@link Texture} object.
     */
    public Texture getNotSelectedTexture() {
        return mNotSelectedTexture;
    }
}
