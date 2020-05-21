package com.esp1920.lookandpick;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;

import com.google.vr.ndk.base.Properties;
import com.google.vr.ndk.base.Value;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * A virtual reality application.
 *
 * <p>This app presents a scene consisting of a room and several floating objects. The user has to pick
 * up objects in order to earn points and get to the next level. This app is meant to be used with a
 * Cardboard viewer: to pick up objects the user must look at them and push the Cardboard trigger button.
 * COMMENTO DA MODIFICARE
 * </p>
 */

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {
    private static final String TAG = "MainActivity";

    //number of objects that can be rendered
    private static final int TARGET_MESH_COUNT = 6;

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 10.0f;

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] FORWARD_VEC = {0.0f, 0.0f, -1.0f, 1.f};

    private static final float MIN_TARGET_DISTANCE = 3.0f;
    private static final float MAX_TARGET_DISTANCE = 3.5f;

    private static final String OBJECT_SOUND_FILE = "audio/HelloVR_Loop.ogg";
    private static final String SUCCESS_SOUND_FILE = "audio/HelloVR_Activation.ogg";

    private static final float DEFAULT_FLOOR_HEIGHT = -1.6f;

    private static final float ANGLE_LIMIT = 0.2f;

    // The maximum yaw and pitch of the target object, in degrees. After hiding the target, its
    // yaw will be within [-MAX_YAW, MAX_YAW] and pitch will be within [-MAX_PITCH, MAX_PITCH].
    private static final float MAX_YAW = 100.0f;
    private static final float MAX_PITCH = 25.0f;

    private static final String[] OBJECT_VERTEX_SHADER_CODE =
            new String[] {
                    "uniform mat4 u_MVP;",
                    "attribute vec4 a_Position;",
                    "attribute vec2 a_UV;",
                    "varying vec2 v_UV;",
                    "",
                    "void main() {",
                    "  v_UV = a_UV;",
                    "  gl_Position = u_MVP * a_Position;",
                    "}",
            };
    private static final String[] OBJECT_FRAGMENT_SHADER_CODE =
            new String[] {
                    "precision mediump float;",
                    "varying vec2 v_UV;",
                    "uniform sampler2D u_Texture;",
                    "",
                    "void main() {",
                    "  // The y coordinate of this sample's textures is reversed compared to",
                    "  // what OpenGL expects, so we invert the y coordinate.",
                    "  gl_FragColor = texture2D(u_Texture, vec2(v_UV.x, 1.0 - v_UV.y));",
                    "}",
            };

    private int objectProgram;

    private int objectPositionParam;
    private int objectUvParam;
    private int objectModelViewProjectionParam;

    private float targetDistance = MAX_TARGET_DISTANCE;

    private TexturedMesh room;
    private Texture roomTex;
    private ArrayList<TexturedMesh> targetObjectMeshes;
    private ArrayList<Texture> targetObjectNotSelectedTextures;
    private ArrayList<Texture> targetObjectSelectedTextures;
    private int curTargetObject;

    private Random random;

    private float[] targetPosition;
    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;

    private float[] modelTarget;
    private float[] modelRoom;

    private float[] tempPosition;
    private float[] headRotation;

    private GvrAudioEngine gvrAudioEngine;
    private volatile int sourceId = GvrAudioEngine.INVALID_ID;
    private volatile int successSourceId = GvrAudioEngine.INVALID_ID;

    private Properties gvrProperties;
    // This is an opaque wrapper around an internal GVR property. It is set via Properties and
    // should be shutdown via a {@link Value#close()} call when no longer needed.
    private final Value floorHeight = new Value();

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        headView = new float[16];

        // Target object first appears directly in front of user.
        targetPosition = new float[] {0.0f, 0.0f, -MIN_TARGET_DISTANCE};
        tempPosition = new float[4];
        headRotation = new float[4];
        modelTarget = new float[16];
        modelRoom = new float[16];

        gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);
    }

    public void initializeGvrView() {
        setContentView(R.layout.activity_main);
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);

        // Chose the EGL config to set element size for RGB, Alpha (opacity), depth and stencil.
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);
        gvrView.enableCardboardTriggerEmulation();
        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }
        setGvrView(gvrView);
        gvrProperties = gvrView.getGvrApi().getCurrentProperties();
    }

    @Override
    public void onPause() {
        super.onPause();
        gvrAudioEngine.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        gvrAudioEngine.resume();
    }

    @Override
    public void onRendererShutdown() {
        floorHeight.close();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     * OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        objectProgram = Util.compileProgram(OBJECT_VERTEX_SHADER_CODE, OBJECT_FRAGMENT_SHADER_CODE);

        objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position");
        objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV");
        objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP");

        // TODO: (per una spiegazione di queste due operazioni ho anche aggiunto un file nel drive)
        // modelRoom is a matrix that contains the coordinates of the room based on user's location.
        Matrix.setIdentityM(modelRoom, 0);
        Matrix.translateM(modelRoom, 0, 0, DEFAULT_FLOOR_HEIGHT, 0);

        // Avoid any delays during start-up due to decoding of sound files.
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Start spatial audio playback of OBJECT_SOUND_FILE at the model position. The
                        // returned sourceId handle is stored and allows for repositioning the sound object
                        // whenever the target position changes.
                        gvrAudioEngine.preloadSoundFile(OBJECT_SOUND_FILE);
                        sourceId = gvrAudioEngine.createSoundObject(OBJECT_SOUND_FILE);
                        gvrAudioEngine.setSoundObjectPosition(
                                sourceId, targetPosition[0], targetPosition[1], targetPosition[2]);
                        gvrAudioEngine.playSound(sourceId, true /* looped playback */);
                        // Preload an unspatialized sound to be played on a successful trigger on the
                        // target.
                        gvrAudioEngine.preloadSoundFile(SUCCESS_SOUND_FILE);
                    }
                })
                .start();

        updateTargetPosition();

        try {
            room = new TexturedMesh(this, "graphics/room/CubeRoom.obj", objectPositionParam, objectUvParam);
            roomTex = new Texture(this, "graphics/room/CubeRoom_BakedDiffuse.png");
            addTargets(objectPositionParam, objectUvParam);
        } catch (IOException e) {
            Log.e(TAG, "Unable to initialize objects", e);
        }

        // Chose randomly the first object to show
        curTargetObject = random.nextInt(TARGET_MESH_COUNT);
    }

    /** Updates the target object position. */
    private void updateTargetPosition() {
        Matrix.setIdentityM(modelTarget, 0);
        Matrix.translateM(modelTarget, 0, targetPosition[0], targetPosition[1], targetPosition[2]);

        // Update the sound location to match it with the new target position.
        if (sourceId != GvrAudioEngine.INVALID_ID) {
            gvrAudioEngine.setSoundObjectPosition(
                    sourceId, targetPosition[0], targetPosition[1], targetPosition[2]);
        }
        Util.checkGlError("updateTargetPosition");
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /** Draw the target object. */
    public void drawTarget() {
    }

    /** Draw the room. */
    public void drawRoom() {
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
    }

    /** Find a new random position for the target object. */
    private void hideTarget() {
    }

    /**
     * Check if user is looking at the target object by calculating where the object is in eye-space.
     *
     * @return true if the user is looking at the target object.
     */
    private boolean isLookingAtTarget() {
        return false;
    }

    private void addTargets(int objectPositionParam, int objectUvParam) throws java.io.IOException{
        targetObjectMeshes = new ArrayList<>();
        targetObjectNotSelectedTextures = new ArrayList<>();
        targetObjectSelectedTextures = new ArrayList<>();
        // TODO: aggiornare variabile TARGET_MESH_COUNT
        targetObjectMeshes.add(
                new TexturedMesh(this, "graphics/obj_models/Icosahedron.obj", objectPositionParam, objectUvParam));
        targetObjectNotSelectedTextures.add(new Texture(this, "graphics/obj_not_selected_textures/Icosahedron_Blue_BakedDiffuse.png"));
        targetObjectSelectedTextures.add(new Texture(this, "graphics/obj_selected_textures/QuadSphere_Pink_BakedDiffuse.png"));
        targetObjectMeshes.add(
                new TexturedMesh(this, "graphics/obj_models/QuadSphere.obj", objectPositionParam, objectUvParam));
        targetObjectNotSelectedTextures.add(new Texture(this, "graphics/obj_not_selected_textures/QuadSphere_Blue_BakedDiffuse.png"));
        targetObjectSelectedTextures.add(new Texture(this, "graphics/obj_selected_textures/QuadSphere_Pink_BakedDiffuse.png"));
        targetObjectMeshes.add(
                new TexturedMesh(this, "graphics/obj_models/TriSphere.obj", objectPositionParam, objectUvParam));
        targetObjectNotSelectedTextures.add(new Texture(this, "graphics/obj_not_selected_textures/TriSphere_Blue_BakedDiffuse.png"));
        targetObjectSelectedTextures.add(new Texture(this, "graphics/obj_not_selected_textures/TriSphere_Blue_BakedDiffuse.png"));
    }
}
