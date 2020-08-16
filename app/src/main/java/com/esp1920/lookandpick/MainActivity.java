package com.esp1920.lookandpick;

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
    private final static String NEW_LINE = "\n";

    // Useful constants which indicate the time expressed in seconds.
    private final static int FIRST_LEVEL_DURATION = 30;
    private final static int SECOND_LEVEL_DURATION = 30;
    private final static int THIRD_LEVEL_DURATION = 30;
    private final static int TIME_BEFORE_RESTART = 10;

    // Used to convert seconds to millis.
    private final static int MILLIS = 1000;

    // Number of objects that can be rendered.
    private static final int TARGET_MESH_COUNT = 8;
    private static final int TARGET_NUMBER = 6;

    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 20.0f;

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] FORWARD_VEC = {0.0f, 0.0f, -1.0f, 1.f};

    private static final String OBJECT_SOUND_FILE = "audio/HelloVR_Loop.ogg";
    private static final String SUCCESS_SOUND_FILE = "audio/HelloVR_Activation.ogg";

    private static final float DEFAULT_FLOOR_HEIGHT = -3.0f;

    private static final float ANGLE_LIMIT = 0.2f;

    // GL shader programs used to render objects.
    private static String[] OBJECT_VERTEX_SHADER_CODE;
    private static String[] OBJECT_FRAGMENT_SHADER_CODE;

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

    // Array that contains the position of each PickableTarget object.
    private PickableTarget[] mPickableTargets;
    // ArrayList which contains the right mesh index of PickableTarget object.
    private ArrayList<Target> mTargets;

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

    // Used to manage all target-related operations.
    private TargetManager mTargetManager = TargetManager.getInstance();

    // This is the default value of the objects' timer in seconds.
    private int defaultTime = 20;

    // Used to manage score and remaining lives.
    private GameStatus gameStatus;
    private StatusManager prefManager;
    private final static int INITIAL_SCORE = 0;
    private final static int NUMBER_OF_LIVES = 3;
    private boolean gameOver;

    private Level mLevel;
    private Handler mHandler;

    private PlayerMovement mPlayerMovement = new PlayerMovement();
    private String s;
    private float eyeZ = 0.0f;

    // Customized TextViews to render and display a string on the scene.
    private VrTextView scoreTv;
    private VrTextView msgTv;
    private VrTextView finalStatusTv;

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();

        OBJECT_VERTEX_SHADER_CODE = getApplicationContext().getResources().getStringArray(R.array.vertex_shader_code);
        OBJECT_FRAGMENT_SHADER_CODE = getApplicationContext().getResources().getStringArray(R.array.fragment_shader_code);

        // Initializes the first level.
        mLevel = new Level(FIRST_LEVEL_DURATION);
        // Initializes the handler to manage the switching between levels.
        mHandler = new Handler();

        gameStatus = new GameStatus(INITIAL_SCORE, NUMBER_OF_LIVES, getApplicationContext());
        prefManager = StatusManager.getInstance(getApplicationContext());

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

        gvrAudioEngine = new GvrAudioEngine(this, GvrAudioEngine.RenderingMode.BINAURAL_HIGH_QUALITY);

        scoreTv = (VrTextView) findViewById(R.id.score);
        msgTv = (VrTextView) findViewById(R.id.msg);
        finalStatusTv = (VrTextView) findViewById(R.id.gameover);

        gameOver = false;

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

        // Builds a GL shader program using vertex and fragment shaders as arrays of strings.
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

        // Updates sound position for the first time.
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
            mPickableTargets[i].setTarget(mTargets.get(mPickableTargets[i].getMeshIndex()));
            Log.d(TAG, "*******primi oggetti " + i + " ********");
        }
        // Manages the transition to the next levels.
        changeLevel();
    }

    /**
     * Updates the sounds' positions.
     *
     * @param pickableTarget The object to which associate the sound.
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
        // Updates eye position along the Z axis to perform movement.
        eyeZ = mPlayerMovement.updateEyePosition(headTransform, eyeZ);

        // Builds the camera matrix and applies it to the ModelView.
        // This matrix determines what the user looks at.
        Matrix.setLookAtM(camera, 0, 0, 0, eyeZ, 0.0f, 0.0f, -1f, 0.0f, 1.0f, 0.0f);

        // Controls if the floor height is available.
        // If true the modelRoom matrix is prepared to be used on onDrawEye method.
        if (gvrProperties.get(PropertyType.TRACKING_FLOOR_HEIGHT, floorHeight)) {
            // The floor height can change each frame when tracking system detects a new floor position.
            roomPosition.setPosition(0, floorHeight.asFloat(), 0);
        } // else the device doesn't support floor height detection so DEFAULT_FLOOR_HEIGHT is used.

        // Writes into headView the transform from the camera space to the head space.
        headTransform.getHeadView(headView, 0);

        // Translate the headView matrix by the vector (0, 0, -eyeZ).
        // Note that moving forward by eyeZ, along the Z axis, means that the "world" has to be
        // translated backward by eyeZ (or translated forward by -eyeZ), along the Z axis.
        Matrix.translateM(headView, 0, 0, 0, -eyeZ);
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

        // Sets modelView for the room, so it's drawn in the correct location.
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
        // If the timer of the object is not finished and the game is not over, it draws the objects on the scene.
        if (!gameOver && !pickableTarget.isHidden()) {
            GLES20.glUseProgram(objectProgram);
            GLES20.glUniformMatrix4fv(objectModelViewProjectionParam, 1, false, modelViewProjection, 0);

            if (isLookingAtTarget(pickableTarget)) {
                targetObjectSelectedTextures.get(pickableTarget.getMeshIndex()).bind();
            } else {
                targetObjectNotSelectedTextures.get(pickableTarget.getMeshIndex()).bind();
            }
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
        // Checks all the objects on the scene and hides the one the user is looking at.
        for (int i = 0; i < TARGET_NUMBER; i++)
            if (isLookingAtTarget(mPickableTargets[i])) {
                if (checkCategory(mPickableTargets[i].getTarget().getCategory())) {
                    gameStatus.increaseScore(mPickableTargets[i].getTarget().getScore());
                    Log.d(TAG, "***Score: " + gameStatus.getScore());
                } else {
                    gameStatus.decreaseLives(1);
                    Log.d(TAG, "***Lives: " + gameStatus.getLives());

                    if (gameStatus.isGameOver()) {
                        Log.d(TAG, "***GAME OVER***");
                        gameOver = true;
                        gameEnd();
                        break;
                    }
                }
                // Displays current score and remaining lives.
                showStatus();
                Log.d(TAG, "***Object category picked up: " + mPickableTargets[i].getTarget().getCategory());

                successSourceId = gvrAudioEngine.createStereoSound(SUCCESS_SOUND_FILE);
                gvrAudioEngine.playSound(successSourceId, false /* looping disabled */);

                hideTarget(mPickableTargets[i]);
                checkMesh(mPickableTargets[i]);
                break;
            }
    }

    /**
     * Performs the game over procedure and restarts the app after {@value TIME_BEFORE_RESTART} seconds.
     */
    private void gameEnd() {
        if (mLevel.getLevelNumber() > 2)
            for (int i = 0; i < TARGET_NUMBER; i++) {
                mPickableTargets[i].getTimer().stopAndHide();
            }

        gameStatus.saveCurrentScore();
        showFinalStatus();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                restartGame();
            }
        }, TIME_BEFORE_RESTART * MILLIS);
    }

    /**
     * Restarts the app.
     */
    private void restartGame() {
        Log.d(TAG, "***RESTART GAME***");
        mHandler.removeCallbacksAndMessages(null);
        Intent restart = new Intent(this, MainActivity.class);
        // Before recreating the Main Activity, closes all the activities on top of it
        // (so the intent will be delivered to the MainActivity, which is now on
        // the top of the stack).
        restart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Makes the MainActivity become the start of a new task (group of activities).
        restart.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // Closes the current activity and restarts it (starts a new one).
        finish();
        startActivity(restart);
    }

    /**
     * Shows on the screen the current score and the remaining lives.
     */
    private void showStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTv.showShortToast(getString(R.string.score) + String.valueOf(gameStatus.getScore())
                        + SPACES + getString(R.string.lives) + String.valueOf(gameStatus.getLives()));
            }
        });
    }

    /**
     * Shows on the screen the score and the remaining lives.
     */
    private void showFinalStatus() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Resources res = getResources();
                String finalStatus;
                if (gameOver)
                    finalStatus = getString(R.string.gameover);
                else
                    finalStatus = getString(R.string.time_finished);
                finalStatusTv.showLongToast(finalStatus + NEW_LINE +
                        getString(R.string.score) + String.valueOf(gameStatus.getScore()) + NEW_LINE +
                        getString(R.string.record) + SPACE + prefManager.getCurrentRecord() + NEW_LINE +
                        String.format(res.getString(R.string.restart), TIME_BEFORE_RESTART));
            }
        });
    }

    /**
     * TODO: PER PROVARE HO CAMBIATO IL TEMPO DEL LIVELLO 20 secondi, 10 secondi e 30 secondi rispettivamente
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

        // Two Runnable variables which contains code useful to switch between levels.
        final Runnable change = new Runnable() {
            @Override
            public void run() {
                mLevel.nextLevel();
                mLevel.setCategory(ObjCategory.getRandomCategory());
                msgTv.showLongToast(getString(R.string.level) + SPACE + mLevel.getLevelNumber() +
                        getString(R.string.request) + SPACE + getString(mLevel.getCategory().getDescription()));
                Log.d(TAG, getString(R.string.level) + mLevel.getLevelNumber());
                Log.d(TAG, "***Category: " + mLevel.getCategory());
            }
        };

        final Runnable hide = new Runnable() {
            @Override
            public void run() {
                hideAllTargets();
                mHandler.removeCallbacks(this);
            }
        };

        // From level 1 to level 2.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                change.run();
                mLevel.setDuration(SECOND_LEVEL_DURATION);
                hide.run();
                // From level 2 to level 3.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        change.run();
                        for (int i = 0; i < TARGET_NUMBER; i++)
                            mPickableTargets[i].initializeTimer(defaultTime);
                        mLevel.setDuration(THIRD_LEVEL_DURATION);
                        hide.run();
                        // End the game after a fixed time (otherwise it would be infinite).
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                gameEnd();
                            }
                        }, mLevel.getDuration() * MILLIS);
                    }
                }, mLevel.getDuration() * MILLIS);
            }
        }, mLevel.getDuration() * MILLIS);
    }

    /**
     * Changes all the targets' position and restarts their timer.
     */
    private void hideAllTargets() {
        for (int i = 0; i < TARGET_NUMBER; i++) {
            hideTarget(mPickableTargets[i]);
        }
        // Chooses a random object and changes its mesh, if necessary.
        checkMesh(mPickableTargets[random.nextInt(TARGET_NUMBER)]);
    }

    /**
     * Changes the position of the {@link PickableTarget} object and updates its mesh.
     *
     * @param pickableTarget The object to update.
     */
    private void hideTarget(PickableTarget pickableTarget) {
        Position tempPosition = newPosition();
        pickableTarget.setPosition(tempPosition);

        updateSoundPosition(pickableTarget);

        int newMesh = random.nextInt(TARGET_MESH_COUNT);

        if ((mLevel.getLevelNumber() == 3) && (pickableTarget.getTimer() != null))
            pickableTarget.getTimer().restartTimer();

        pickableTarget.changeMesh(newMesh, mTargets.get(newMesh));
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

            // If the distance is <2.0, calculates a new random position and starts the loop from the beginning.
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
        // If true then there is at least one object of the right category according to the level request.
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
        } while (!checkCategory(mTargets.get(newMesh).getCategory()));

        // Updates the pickableTarget object with the new mesh.
        pickableTarget.changeMesh(newMesh, mTargets.get(newMesh));
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
        mTargets = new ArrayList<>();
        targetObjectMeshes = new ArrayList<>();
        targetObjectNotSelectedTextures = new ArrayList<>();
        targetObjectSelectedTextures = new ArrayList<>();

        mTargets.add(new Target(ObjName.PENGUIN, "graphics/penguin/penguin.obj", "graphics/penguin/dark_penguin.png", "graphics/penguin/penguin.png"));
        mTargets.add(new Target(ObjName.CAT, "graphics/cat/cat.obj", "graphics/cat/dark_cat.png", "graphics/cat/cat.png"));
        mTargets.add(new Target(ObjName.PIKACHU, "graphics/pikachu/pikachu.obj", "graphics/pikachu/dark_pikachu.png", "graphics/pikachu/pikachu.png"));
        mTargets.add(new Target(ObjName.GREEN_ANDROID, "graphics/android/green_android.obj", "graphics/android/dark_green_android.png", "graphics/android/green_android.png"));
        mTargets.add(new Target(ObjName.CACTUS, "graphics/cactus/cactus.obj", "graphics/cactus/dark_cactus.png", "graphics/cactus/cactus.png"));
        mTargets.add(new Target(ObjName.MOUSE, "graphics/mouse/mouse.obj", "graphics/mouse/dark_mouse.png", "graphics/mouse/mouse.png"));
        mTargets.add(new Target(ObjName.PLANE, "graphics/plane/plane.obj", "graphics/plane/dark_plane.png", "graphics/plane/plane.png"));
        mTargets.add(new Target(ObjName.SUNFLOWER, "graphics/sunflower/sunflower.obj", "graphics/sunflower/dark_sunflower.png", "graphics/sunflower/sunflower.png"));

        for (int i = 0; i < TARGET_MESH_COUNT; i++) {
            addObject(mTargets.get(i), objectPositionParam, objectUvParam);
        }
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
