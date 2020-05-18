package com.esp1920.lookandpick;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;

/** A texture, meant for use with TexturedMesh. */
/* package */ class Texture {
    private final int[] textureId = new int[1];

    /**
     * Initializes the texture.
     *
     * @param context Context for loading the texture file.
     * @param texturePath Path to the image to use for the texture.
     */
    public Texture(Context context, String texturePath) throws IOException {

        //generates a name for the texture and stores it in textureId
        GLES20.glGenTextures(1, textureId, 0);

        //binds texture to active texture unit and target
        bind();

        //sets wrap parameters for texture coordinates s and t
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //sets texture minifying and magnifying functions
        GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);


        Bitmap textureBitmap = BitmapFactory.decodeStream(context.getAssets().open(texturePath));
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textureBitmap, 0);
        textureBitmap.recycle();
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    }


    /** Binds the texture to GL_TEXTURE0. */
    public void bind() {

        //activates the texture unit
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //binds texture to a 2-dimensional target (face of the polygon)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
    }
}

