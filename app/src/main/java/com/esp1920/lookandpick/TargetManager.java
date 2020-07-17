package com.esp1920.lookandpick;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * This class provides all necessary methods to manage a {@link Target} object behaviour.
 */
public class TargetManager {
    private final String TAG = "TargetManager";
    private TexturedMesh mTexturedMesh;
    private Texture mNotSelectedTexture;
    private Texture mSelectedTexture;

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
