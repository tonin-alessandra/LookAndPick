package com.esp1920.lookandpick;

import android.content.Context;
import android.opengl.GLES20;

import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * This class has been written by Google and renders an object loaded from an OBJ file.
 * It is taken from gvr-android-sdk-1.200 project, more precisely from sdk-hellovr sample.
 */
/* package */ class TexturedMesh {
    private static final String TAG = "TexturedMesh";

    private final FloatBuffer vertices;
    private final FloatBuffer uv;
    private final ShortBuffer indices;
    private final int positionAttrib;
    private final int uvAttrib;

    /**
     * Creates the polygon mesh from an .obj file.
     *
     * @param context        The context for loading the .obj file.
     * @param objFilePath    The path to the .obj file.
     * @param positionAttrib The position attribute in the shader.
     * @param uvAttrib       The UV attribute in the shader.
     * @throws IOException if unable to find file path.
     */
    public TexturedMesh(Context context, String objFilePath, int positionAttrib, int uvAttrib)
            throws IOException {
        // Gets renderable obj from .obj file.
        InputStream objInputStream = context.getAssets().open(objFilePath);
        Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
        objInputStream.close();

        // Gets vertex indices of the faces of the obj (3 vertices for each face: triangles).
        IntBuffer intIndices = ObjData.getFaceVertexIndices(obj, 3);

        vertices = ObjData.getVertices(obj);
        uv = ObjData.getTexCoords(obj, 2);

        // Converts int indices to shorts (GLES doesn't support int indices).
        indices = ByteBuffer.allocateDirect(2 * intIndices.limit())
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        while (intIndices.hasRemaining()) {
            indices.put((short) intIndices.get());
        }

        // Makes buffer ready for reading the data it contains, sets the position to zero.
        indices.rewind();

        this.positionAttrib = positionAttrib;
        this.uvAttrib = uvAttrib;
    }

    /**
     * Draws the mesh. Before this is called, u_MVP should be set with glUniformMatrix4fv(), and a
     * texture should be bound to GL_TEXTURE0.
     */
    public void draw() {
        GLES20.glEnableVertexAttribArray(positionAttrib);

        // Specifies source and format of vertex attributes.
        // The first param (positionAttrib) is the source buffer.
        // The last one (vertices) is the offset of the first attribute in positionAttrib.
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, vertices);

        GLES20.glEnableVertexAttribArray(uvAttrib);

        // Specifies source and format of attributes regarding UV coordinates.
        GLES20.glVertexAttribPointer(uvAttrib, 2, GLES20.GL_FLOAT, false, 0, uv);

        // Draws the triangle mesh.
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.limit(), GLES20.GL_UNSIGNED_SHORT, indices);
    }
}
