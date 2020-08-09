package com.esp1920.lookandpick;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.vr.ndk.base.Properties;
import com.google.vr.ndk.base.Value;
import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import com.google.vr.ndk.base.Properties.PropertyType;

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
 * TODO: COMMENTO DA MODIFICARE
 * </p>
 */

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {
    private static final String TAG = "MainActivity";

    private final static String SPACE = " ";
    private final static String SPACES = "     ";
    //Used to convert seconds to millis.
    private final static int MILLIS = 1000;

    // Number of objects that can be rendered.
    private static final int TARGET_MESH_COUNT = 8;
    private static final int TARGET_NUMBER = 6;

    // TODO: change these values to change how far user can see
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 20.0f;

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] FORWARD_VEC = {0.0f, 0.0f, -1.0f, 1.f};

    private static final String OBJECT_SOUND_FILE = "audio/HelloVR_Loop.ogg";
    private static final String SUCCESS_SOUND_FILE = "audio/HelloVR_Activation.ogg";

    private static final float DEFAULT_FLOOR_HEIGHT = -3.0f;

    private static final float ANGLE_LIMIT = 0.2f;
    private static final String[] OBJECT_VERTEX_SHADER_CODE =
            new String[]{
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
            new String[]{
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

    private Target room;
    private TexturedMesh roomTextureMesh;
    private Texture roomTexture;
    private ArrayList<TexturedMesh> targetObjectMeshes;
    private ArrayList<Texture> targetObjectNotSelectedTextures;
    private ArrayList<Texture> targetObjectSelectedTextures;

    private Random random;

    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;

    // Array where are stored position and index of each pickable object.
    private PickableTarget[] mPickableTargets;
    private Target[] mTargets;

    private Position roomPosition;

    private float[] tempPosition;
    private float[] headRotation;

    // Used to initialize Google VR Audio Engine.
    private GvrAudioEngine gvrAudioEngine;
    private volatile int sourceId = GvrAudioEngine.INVALID_ID;
    private volatile int successSourceId = GvrAudioEngine.INVALID_ID;

    private Properties gvrProperties;

    // This is an opaque wrapper around an internal GVR property. It is set via Properties and
    // should be shutdown via a {@link Value#close()} call when no longer needed.
    private final Value floorHeight = new Value();

    // Used to manage all target-related operations
    private TargetManager mTargetManager = TargetManager.getInstance();


    // This is the default value of the objects' timer in seconds
    private int defaultTime = 20;

    // Used to manage score and remaining lives (gameover)
    private GameStatus gameStatus;
    private final static int INITIAL_SCORE = 0;
    private final static int NUMBER_OF_LIVES = 3;

    private Level mLevel;
    private Handler mHandler;

    private PlayerMovement mPlayerMovement = new PlayerMovement();

    private float eyeZ = 0.0f;

    private VrTextView scoreTv;
    private VrTextView msgTv;

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();

        // Initializes the first level
        mLevel = new Level();
        // Initializes the handler to manage the switching between levels
        mHandler = new Handler();

        gameStatus = new GameStatus(INITIAL_SCORE, NUMBER_OF_LIVES, getApplicationContext());

        random = new Random();

        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        headView = new float[16];

        // Creates TARGET_NUMBER pickable objects on the scene without any associated mesh with a
        // random position.
        mPickableTargets = new PickableTarget[TARGET_NUMBER];
        for (int i = 0; i < TARGET_NUMBER; i++)
            mPickableTargets[i] = new PickableTarget();

        // Changes the position of each pickable target in order to avoid overlapping.
        for (int i = 0; i < TARGET_NUMBER; i++)
            mPickableTargets[i].setPosition(newPosition());

        tempPosition = new float[4];
        headRotation = new float[4];
        roomPosition = new Position();

        mTargets = new Target[TARGET_MESH_COUNT];

        gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);

        scoreTv = (VrTextView) findViewById(R.id.score);

        msgTv = (VrTextView) findViewById(R.id.msg);
        // msgTv.showLongToast("Level 0 \n Pick up as many objects as you can!");
    }

    /**
     * TODO: write specification
     */
    public void initializeGvrView() {
        setContentView(R.layout.activity_main);
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);

        // Chooses the EGL config to set element size for RGB, Alpha (opacity), depth and stencil.
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(true);

        setGvrView(gvrView);
        gvrProperties = gvrView.getGvrApi().getCurrentProperties();

    }

    @Override
    public void onPause() {
        gvrAudioEngine.pause();
        super.onPause();
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
     * OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Builds a GL shader program using vertex and fragment shaders as arrays of strings
        objectProgram = Util.compileProgram(OBJECT_VERTEX_SHADER_CODE, OBJECT_FRAGMENT_SHADER_CODE);

        objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position");
        objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV");

        // Returns the location of the uniform variable u_MVP within the program 'objectProgram'.
        objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP");

        roomPosition.setPosition(0, DEFAULT_FLOOR_HEIGHT, 0);

        // Avoid any delays during start-up due to decoding of sound files.
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Starts spatial audio playback of OBJECT_SOUND_FILE at the model position. The
                        // returned sourceId handle is stored and allows for repositioning the sound object
                        // whenever the target position changes.
                        gvrAudioEngine.preloadSoundFile(OBJECT_SOUND_FILE);
                        sourceId = gvrAudioEngine.createSoundObject(OBJECT_SOUND_FILE);
                        for (int i = 0; i < TARGET_NUMBER; i++)
                            gvrAudioEngine.setSoundObjectPosition(
                                    sourceId,
                                    mPickableTargets[i].getPosition().getXCoordinate(),
                                    mPickableTargets[i].getPosition().getYCoordinate(),
                                    mPickableTargets[i].getPosition().getZCoordinate());
                        gvrAudioEngine.playSound(sourceId, true /* looped playback */);
                        // Preload an unspatialized sound to be played on a successful trigger on the
                        // target.
                        gvrAudioEngine.preloadSoundFile(SUCCESS_SOUND_FILE);
                    }
                })
                .start();

        // Updates sound position for the first time
        for (int i = 0; i < TARGET_NUMBER; i++)
            updateSoundPosition(mPickableTargets[i]);

        Util.checkGlError("onSurfaceCreated");

        try {
            room = new Target(ObjName.ROOM, "graphics/room/BigCubeRoom.obj", "graphics/room/BigCubeRoom.png", "graphics/room/BigCubeRoom.png");
            mTargetManager.applyTexture(this, room, objectPositionParam, objectUvParam);
            roomTextureMesh = mTargetManager.getTexturedMesh();
            roomTexture = mTargetManager.getSelectedTexture();
            addTargets(objectPositionParam, objectUvParam);
        } catch (IOException e) {
            Log.e(TAG, "Unable to initialize objects", e);
        }

        // Chooses randomly the first object to show for each pickable object.
        for (int i = 0; i < TARGET_NUMBER; i++) {
            mPickableTargets[i].setMeshIndex(random.nextInt(TARGET_MESH_COUNT));
            mPickableTargets[i].setTarget(mTargets[mPickableTargets[i].getMeshIndex()]);
            Log.d(TAG, "*******primi oggetti " + i + " ********");
        }
        // Manages the transition to the next levels.
        changeLevel();
    }

    /**
     * Updates the sounds' positions.
     */
    private void updateSoundPosition(PickableTarget pickableTarget) {
        // Updates the sound location to match it with the new target position.
        if (sourceId != GvrAudioEngine.INVALID_ID) {
            gvrAudioEngine.setSoundObjectPosition(
                    sourceId, pickableTarget.getPosition().getXCoordinate(),
                    pickableTarget.getPosition().getYCoordinate(),
                    pickableTarget.getPosition().getZCoordinate());
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
        // Updates eye position along z axis to perform movement
        eyeZ = mPlayerMovement.updateEyePosition(headTransform, eyeZ);

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0, 0, eyeZ, 0.0f, 0.0f, -1f, 0.0f, 1.0f, 0.0f);

        // Controls if the floor height is available.
        // If true the modelRoom matrix is prepared to be used on onDrawEye method.
        if (gvrProperties.get(PropertyType.TRACKING_FLOOR_HEIGHT, floorHeight)) {
            // The floor height can change each frame when tracking system detects a new floor position.
            roomPosition.setPosition(0, floorHeight.asFloat(), 0);
        } // else the device doesn't support floor height detection so DEFAULT_FLOOR_HEIGHT is used.

        // Writes into headView the transform from the camera space to the head space
        headTransform.getHeadView(headView, 0);

        // Updates the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);
        gvrAudioEngine.setHeadRotation(
                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
        // Regular update call to GVR audio engine.
        gvrAudioEngine.update();
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // The clear color doesn't matter here because it's completely obscured by
        // the room. However, the color buffer is still cleared because it may
        // improve performance.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Applies the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);

        // Builds the ModelView and ModelViewProjection matrices
        // for calculating the position of the target object.
        for (int i = 0; i < TARGET_NUMBER; i++) {
            Matrix.multiplyMM(modelView, 0, view, 0, mPickableTargets[i].getPosition().getModel(), 0);
            Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
            drawTarget(mPickableTargets[i]);
        }

        // Sets modelView for the room, so it's drawn in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, roomPosition.getModel(), 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawRoom();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {
    }

    /**
     * Draws the target object.
     *
     * @param pickableTarget The PickableTarget object to draw.
     */
    public void drawTarget(PickableTarget pickableTarget) {
        GLES20.glUseProgram(objectProgram);
        GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0);
        if (isLookingAtTarget(pickableTarget)) {
            targetObjectSelectedTextures.get(pickableTarget.getMeshIndex()).bind();
        } else {
            targetObjectNotSelectedTextures.get(pickableTarget.getMeshIndex()).bind();
        }
        if (!(pickableTarget.isHidden())) {
            targetObjectMeshes.get(pickableTarget.getMeshIndex()).draw();
        }
    }

    /**
     * Draws the room using the GL shader program created before.
     */
    public void drawRoom() {
        GLES20.glUseProgram(objectProgram);
        // TODO: write better this explanation!
        /*
          Specifies the value of a uniform matrix for the program object.
          Uses values contained in float[16] modelViewProjection to fill a 4x4 matrix.
          The first parameter is the location of the uniform variable to be modified (u_MVP).
          The other is the value used to update the variable.
        */
        GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0);
        roomTexture.bind();
        roomTextureMesh.draw();
        Util.checkGlError("drawRoom");
    }

    /**
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        // TODO: add a message if the user doesn't hit the target (?) (like the other project)

        // Checks all the targets and hides the one the user is looking at.
        for (int i = 0; i < TARGET_NUMBER; i++)
            if (isLookingAtTarget(mPickableTargets[i])) {
                if (checkCategory(mPickableTargets[i].getTarget().getCategory())) {
                    gameStatus.increaseScore(mPickableTargets[i].getTarget().getScore());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scoreTv.showShortToast(getString(R.string.score) + String.valueOf(gameStatus.getScore())
                                    + SPACES + getString(R.string.lives) + String.valueOf(gameStatus.getLives()));
                        }
                    });
                    Log.d(TAG, "***Score: " + gameStatus.getScore());
                } else {
                    gameStatus.decreaseLives(1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scoreTv.showShortToast(getString(R.string.score) + String.valueOf(gameStatus.getScore())
                                    + SPACES + getString(R.string.lives) + String.valueOf(gameStatus.getLives()));
                        }
                    });
                    Log.d(TAG, getString(R.string.lives) + gameStatus.getLives());

                    if (gameStatus.gameOver()) {
                        // GAME OVER
                        Log.d(TAG, "***GAME OVER***");
                        gameStatus.saveCurrentScore();
                        // TODO: show a TextView with Gameover and score
                        //       make objects disappear from the scene
                        Intent restart = new Intent(this, MainActivity.class);
                        // Before recreating the Main Activity, closes all the activities on top of it
                        // (so the intent will be delivered to the MainActivity, which is now on
                        // the top of the stack).
                        restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        // Makes the MainActivity become the start of a new task (group of activities)
                        restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // Closes the current activity and restarts it (starts a new one).
                        finish();
                        startActivity(restart);
                    }
                }

                Log.d(TAG, "***Object category picked up: " + mPickableTargets[i].getTarget().getCategory());

                successSourceId = gvrAudioEngine.createStereoSound(SUCCESS_SOUND_FILE);
                gvrAudioEngine.playSound(successSourceId, false /* looping disabled */);

                mPickableTargets[i].setMeshIndex(hideTarget(mPickableTargets[i]));
                mPickableTargets[i].setTarget(mTargets[mPickableTargets[i].getMeshIndex()]);

                checkMesh(mPickableTargets[i]);
                break;
            }
    }

//    /**
//     * Shows on the screen the score and the remaining lives.
//     */
//    private void showStatus() {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                scoreTv.showShortToast(getString(R.string.score) + String.valueOf(gameStatus.getScore())
//                        + getString(R.string.spaces) + getString(R.string.lives) + String.valueOf(gameStatus.getLives()));
//            }
//        });
//    }

    /**
     * TODO: use real level duration, here I used 20 seconds and 60 seconds to try.
     * Handles the change of level, changing parameters after a fixed amount of time (which is the level duration).
     * Since there are 3 different levels, when the duration time of the first one is reached, there is
     * a switch to the second one. Same thing for the third level.
     */
    private void changeLevel() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgTv.showLongToast(getString(R.string.level) + SPACE + mLevel.getLevelNumber() + getString(R.string.request) + SPACE + getString(R.string.all));
            }
        });
//        Runnable changeLvl = new Runnable(){
//            @Override
//            public void run() {
//                mLevel.nextLevel();
//                mLevel.setCategory(ObjCategory.getRandomCategory());
//                msgTv.showLongToast(getString(R.string.level) + SPACE + Level.getLevelNumber() +
//                        getString(R.string.request) + SPACE + getString(mLevel.getCategory().getDescription()));
//                if(Level.getLevelNumber() ==2){
//                mLevel.setDuration(60);}
//                if(Level.getLevelNumber() == 3){
//                    for (int i = 0; i < TARGET_NUMBER; i++)
//                        mPickableTargets[i].initializeTimer(defaultTime);
//                }
//                Log.d(TAG, getString(R.string.level) + Level.getLevelNumber());
//                Log.d(TAG, "***Category: " + mLevel.getCategory());
//                hideAllTargets();
//                mHandler.removeCallbacks(this);
//
//            }
//        };
//
//        mHandler.postDelayed(changeLvl,20000 );
//        mHandler.postDelayed(changeLvl,mLevel.getDuration() * MILLIS );


        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //*********************************
                mLevel.nextLevel();
                mLevel.setCategory(ObjCategory.getRandomCategory());
                msgTv.showLongToast(getString(R.string.level) + SPACE + mLevel.getLevelNumber() +
                        getString(R.string.request) + SPACE + getString(mLevel.getCategory().getDescription()));
                //*******************************

                mLevel.setDuration(60);
                ///*************************
                Log.d(TAG, getString(R.string.level) + mLevel.getLevelNumber());
                Log.d(TAG, "***Category: " + mLevel.getCategory());
                hideAllTargets();
                //************
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLevel.nextLevel();
                        mLevel.setCategory(ObjCategory.getRandomCategory());
                        msgTv.showLongToast(getString(R.string.level) + mLevel.getLevelNumber() +
                                getString(R.string.request) + SPACE + getString(mLevel.getCategory().getDescription()));

                        for (int i = 0; i < TARGET_NUMBER; i++)
                            mPickableTargets[i].initializeTimer(defaultTime);

                        Log.d(TAG, getString(R.string.level) + mLevel.getLevelNumber());
                        Log.d(TAG, "***Category: " + mLevel.getCategory());
                        hideAllTargets();
                    }
                }, mLevel.getDuration() * MILLIS); //Level duration is in seconds, but handler requires millis
            }
        }, 20000);
    }

    /**
     * Changes all the targets' position and restarts their timer.
     */
    private void hideAllTargets() {
        for (int i = 0; i < TARGET_NUMBER; i++) {
            mPickableTargets[i].setMeshIndex(hideTarget(mPickableTargets[i]));
            mPickableTargets[i].setTarget(mTargets[mPickableTargets[i].getMeshIndex()]);
        }
        // Chooses a random object and changes its mesh, if necessary.
        checkMesh(mPickableTargets[random.nextInt(TARGET_NUMBER)]);
    }

    /**
     * Finds a new random position for the target object.
     */
    private int hideTarget(PickableTarget pickableTarget) {
        Position tempPosition = newPosition();
        pickableTarget.setPosition(tempPosition);

        updateSoundPosition(pickableTarget);

        int newMesh = random.nextInt(TARGET_MESH_COUNT);

        if ((mLevel.getLevelNumber() == 3) && (pickableTarget.getTimer() != null))
            pickableTarget.getTimer().restartTimer();

        return newMesh;
    }

    /**
     * Generates a new Position with a distance of at least 2.0 from the other objects.
     *
     * @return The new {@link Position}.
     */
    private Position newPosition() {
        float distance;

        Position tempPosition = new Position();
        tempPosition.generateRandomPosition();

        float x1 = tempPosition.getXCoordinate();
        float y1 = tempPosition.getYCoordinate();
        float z1 = tempPosition.getZCoordinate();

        for (int i = 0; i < TARGET_NUMBER; i++) {
            float x2 = mPickableTargets[i].getPosition().getXCoordinate();
            float y2 = mPickableTargets[i].getPosition().getYCoordinate();
            float z2 = mPickableTargets[i].getPosition().getZCoordinate();

            // Calculates the Euclidean distance between the new position and all the pickableTarget objects.
            distance = (float) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2) + Math.pow((z1 - z2), 2));

            // If the distance is <2.0 then calculate a new random position and start the loop from the beginning
            if (distance < 2.0) {
                tempPosition.generateRandomPosition();
                x1 = tempPosition.getXCoordinate();
                y1 = tempPosition.getYCoordinate();
                z1 = tempPosition.getZCoordinate();
                i = 0;
            }
        }

        return tempPosition;
    }

    /**
     * Controls if the mesh of the {@link PickableTarget} object passed belongs to the same category
     * of the level. If there are no objects belonging to the level category a new mesh will be
     * calculated in order to have at least one object with the right category.
     *
     * @param pickableTarget The {@link PickableTarget} object to control.
     */
    private void checkMesh(PickableTarget pickableTarget) {
        // If true then there is at least one object of the same category of the level.
        if (checkCategory(pickableTarget.getTarget().getCategory()))
            return;

        // Controls if there is an object with the same category of the level.
        for (int i = 0; i < TARGET_NUMBER; i++) {
            if (checkCategory(mPickableTargets[i].getTarget().getCategory()))
                return;
        }

        int newMesh;
        // Changes the mesh of the pickableTarget with a new one until it belongs to the level category.
        do {
            newMesh = random.nextInt(TARGET_MESH_COUNT);
        } while (!checkCategory(mTargets[newMesh].getCategory()));

        // Updates the pickableTarget object with the new mesh
        pickableTarget.setMeshIndex(newMesh);
        pickableTarget.setTarget(mTargets[newMesh]);
    }

    /**
     * Checks if user is looking at the target object by calculating where the object is in eye-space.
     *
     * @return true if the user is looking at the target object.
     */
    private boolean isLookingAtTarget(PickableTarget pickableTarget) {
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, pickableTarget.getPosition().getModel(), 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);

        float angle = Util.angleBetweenVectors(tempPosition, FORWARD_VEC);
        return angle < ANGLE_LIMIT;
    }

    /**
     * Checks if the category passed as param is the same of the level's category.
     *
     * @param obj The category of the {@link PickableTarget} to check.
     * @return true if the categories match, false otherwise.
     */
    private boolean checkCategory(ObjCategory obj) {
        // If the level's category is ALL, it means all objects are correct.
        if (mLevel.getCategory() == ObjCategory.ALL) return true;
        return obj == mLevel.getCategory();
    }

    /**
     * Adds 3D objects to the scene.
     *
     * @param objectPositionParam The position attribute in the shader.
     * @param objectUvParam       The UV attribute in the shader.
     * @throws IOException if unable to initialize objects.
     */
    private void addTargets(int objectPositionParam, int objectUvParam) throws IOException {
        targetObjectMeshes = new ArrayList<>();
        targetObjectNotSelectedTextures = new ArrayList<>();
        targetObjectSelectedTextures = new ArrayList<>();

        Target tarPenguin = new Target(ObjName.PENGUIN, "graphics/penguin/penguin.obj", "graphics/penguin/dark_penguin.png", "graphics/penguin/penguin.png");
        Target tarCat = new Target(ObjName.CAT, "graphics/cat/cat.obj", "graphics/cat/dark_cat.png", "graphics/cat/cat.png");
        Target tarPikachu = new Target(ObjName.PIKACHU, "graphics/pikachu/pikachu.obj", "graphics/pikachu/dark_pikachu.png", "graphics/pikachu/pikachu.png");
        Target tarAndroid = new Target(ObjName.GREEN_ANDROID, "graphics/android/green_android.obj", "graphics/android/dark_green_android.png", "graphics/android/green_android.png");
        Target tarCactus = new Target(ObjName.CACTUS, "graphics/cactus/cactus.obj", "graphics/cactus/dark_cactus.png", "graphics/cactus/cactus.png");
        Target tarMouse = new Target(ObjName.MOUSE, "graphics/mouse/mouse.obj", "graphics/mouse/dark_mouse.png", "graphics/mouse/mouse.png");
        Target tarPlane = new Target(ObjName.PLANE, "graphics/plane/plane.obj", "graphics/plane/dark_plane.png", "graphics/plane/plane.png");
        Target tarSunflower = new Target(ObjName.SUNFLOWER, "graphics/sunflower/sunflower.obj", "graphics/sunflower/dark_sunflower.png", "graphics/sunflower/sunflower.png");

        mTargets[0] = tarCat;
        addObject(tarCat, objectPositionParam, objectUvParam);
        mTargets[1] = tarPikachu;
        addObject(tarPikachu, objectPositionParam, objectUvParam);
        mTargets[2] = tarPenguin;
        addObject(tarPenguin, objectPositionParam, objectUvParam);
        mTargets[3] = tarAndroid;
        addObject(tarAndroid, objectPositionParam, objectUvParam);
        mTargets[4] = tarCactus;
        addObject(tarCactus, objectPositionParam, objectUvParam);
        mTargets[5] = tarMouse;
        addObject(tarMouse, objectPositionParam, objectUvParam);
        mTargets[6] = tarPlane;
        addObject(tarPlane, objectPositionParam, objectUvParam);
        mTargets[7] = tarSunflower;
        addObject(tarSunflower, objectPositionParam, objectUvParam);
    }

    /**
     * Initializes a 3D object with the correct texture, according to the given params.
     *
     * @param target              The target object to initialize.
     * @param objectPositionParam The position attribute in the shader.
     * @param objectUvParam       The UV attribute in the shader.
     */
    private void addObject(Target target, int objectPositionParam, int objectUvParam) {
        mTargetManager.applyTexture(this, target, objectPositionParam, objectUvParam);
        targetObjectMeshes.add(mTargetManager.getTexturedMesh());
        targetObjectNotSelectedTextures.add(mTargetManager.getNotSelectedTexture());
        targetObjectSelectedTextures.add(mTargetManager.getSelectedTexture());
    }

}
