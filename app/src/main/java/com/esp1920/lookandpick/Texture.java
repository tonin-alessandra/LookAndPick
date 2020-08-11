package com.esp1920.lookandpick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;

/**
 * This class has been written by Google and represents a texture, meant for use with TexturedMesh.
 * It is taken from gvr-android-sdk-1.200 project, more precisely from sdk-hellovr sample.
 */
/* package */ class Texture {
    private final int[] textureId = new int[1];

    /**
     * Initializes the texture.
     *
     * @param context     Context for loading the texture file.
     * @param texturePath Path to the image to use for the texture.
     */
    public Texture(Context context, String texturePath) throws IOException {
        // Generates a name for the texture and stores it in textureId
        GLES20.glGenTextures(1, textureId, 0);

        // Binds texture to active texture unit and target
        bind();

        // Sets wrap parameters for texture coordinates s and t
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        // Sets texture minifying and magnifying functions
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Creates a bitmap from the texture given
        Bitmap textureBitmap = BitmapFactory.decodeStream(context.getAssets().open(texturePath));

        // Specifies the texture for the current texture unit and generates a MIP map
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
        textureBitmap.recycle();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

    }

    /**
     * Binds the texture to GL_TEXTURE0.
     */
    public void bind() {
        // Activates texture unit
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Binds texture to two-dimensional target (face of polygon)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
    }
}

