package com.esp1920.lookandpick;

import android.content.Context;
import android.opengl.GLES20;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/** Renders an object loaded from an OBJ file. */
/* package */ class TexturedMesh {
    private static final String TAG = "TexturedMesh";

    private final FloatBuffer vertices;
    private final FloatBuffer uv;
    private final ShortBuffer indices;
    private final int positionAttrib;
    private final int uvAttrib;

    /**
     * Initializes the mesh from an .obj file.
     *
     * @param context Context for loading the .obj file.
     * @param objFilePath Path to the .obj file.
     * @param positionAttrib The position attribute in the shader.
     * @param uvAttrib The UV attribute in the shader.
     */
    public TexturedMesh(Context context, String objFilePath, int positionAttrib, int uvAttrib) throws IOException {
    }

    /**
     * Draws the mesh. Before this is called, u_MVP should be set with glUniformMatrix4fv(), and a
     * texture should be bound to GL_TEXTURE0.
     */
    public void draw() {
    }
}
