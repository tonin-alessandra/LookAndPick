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
    }

    /** Binds the texture to GL_TEXTURE0. */
    public void bind() {
    }
}

