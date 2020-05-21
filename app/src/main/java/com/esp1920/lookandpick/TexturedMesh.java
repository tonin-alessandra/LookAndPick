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

/** Renders an object loaded from an OBJ file. */
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
     * @param context Context for loading the .obj file.
     * @param objFilePath Path to the .obj file.
     * @param positionAttrib The position attribute in the shader.
     * @param uvAttrib The UV attribute in the shader.
     */
    public TexturedMesh(Context context, String objFilePath, int positionAttrib, int uvAttrib)
            throws IOException {

        // Get renderable obj from .obj file
        InputStream objInputStream = context.getAssets().open(objFilePath);
        Obj obj = ObjUtils.convertToRenderable(ObjReader.read(objInputStream));
        objInputStream.close();

        // Get vertex indices of the faces of the obj (3 vertices for each face: triangles)
        IntBuffer intIndices = ObjData.getFaceVertexIndices(obj, 3);

        vertices = ObjData.getVertices(obj);
        uv = ObjData.getTexCoords(obj, 2);

        // Convert int indices to shorts (GLES doesn't support int indices)
        indices =
                ByteBuffer.allocateDirect(2 * intIndices.limit())
                        .order(ByteOrder.nativeOrder())
                        .asShortBuffer();
        while (intIndices.hasRemaining()) {
            indices.put((short) intIndices.get());
        }

        // Make buffer ready for reading the data it contains, sets the position to zero
        indices.rewind();

        this.positionAttrib = positionAttrib;
        this.uvAttrib = uvAttrib;
    }

    /**
     * Draws the mesh. [Before this is called, u_MVP should be set with glUniformMatrix4fv()?], and a
     * texture should be bound to GL_TEXTURE0.
     */
    public void draw() {

        GLES20.glEnableVertexAttribArray(positionAttrib);

        // Specify from where and how to read vertex attributes
        // positionAttrib is the source buffer
        // vertices is the offset of the first attribute in positionAttrib
        GLES20.glVertexAttribPointer(positionAttrib, 3, GLES20.GL_FLOAT, false, 0, vertices);

        GLES20.glEnableVertexAttribArray(uvAttrib);

        // Specify from where and how to read attributes regarding UV coordinates
        GLES20.glVertexAttribPointer(uvAttrib, 2, GLES20.GL_FLOAT, false, 0, uv);

        // Draw the triangle mesh
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.limit(), GLES20.GL_UNSIGNED_SHORT, indices);
    }
}
