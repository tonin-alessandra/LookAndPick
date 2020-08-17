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
 * LookAndPick is a virtual reality application.
 *
 * <p>It presents a scene consisting of a room and several floating objects. The user has to pick
 * up them, in order to earn points and get to the next level. This app is meant to be used with a
 * Cardboard viewer: to pick up objects, the user must look at them and push the Cardboard trigger button.
 * The player can also move backward and forward in the room.
 * </p>
 */

public class MainActivity extends GvrActivity implements GvrView.StereoRenderer {
    private static final String TAG = "MainActivity";

    private final static String SPACE = " ";
    private final static String SPACES = "     ";
    private final static String NEW_LINE = "\n";

    // Useful constants which indicate the time expressed in seconds.
    private final static int FIRST_LEVEL_DURATION = 20;
    private final static int SECOND_LEVEL_DURATION = 30;
    private final static int THIRD_LEVEL_DURATION = 40;
    private final static int TIME_BEFORE_RESTART = 10;

    // Player has 3 lives to increase the score and complete the game.
    private final static int INITIAL_SCORE = 0;
    private final static int NUMBER_OF_LIVES = 3;

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

    private int objectModelViewProjectionParam;

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
    // ArrayList which contains the right mesh index of each PickableTarget object.
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
    private boolean gameOver;

    // Used to manage the switching between levels.
    private Level mLevel;
    private Handler mHandler;

    // Allows the player to move in the room.
    private PlayerMovement mPlayerMovement = new PlayerMovement();
    private float eyeZ = 0.0f;

    // Customized TextViews, to render and display a string on the scene.
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
        mHandler = new Handler();
        gameStatus = new GameStatus(INITIAL_SCORE, NUMBER_OF_LIVES, getApplicationContext());
        prefManager = StatusManager.getInstance(getApplicationContext());

        random = new Random();

        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        headView = new float[16];

        // Creates TARGET_NUMBER pickable objects on the scene without any associated mesh and with
        // a random position.
        mPickableTargets = new PickableTarget[TARGET_NUMBER];
        for (int i = 0; i < TARGET_NUMBER; i++)
            mPickableTargets[i] = new PickableTarget();

        // Changes the position of each PickableTarget object in order to avoid overlapping.
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
     * Initializes the {@link GvrView} and sets the activity's layout.
     */
    public void initializeGvrView() {
        setContentView(R.layout.activity_main);
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        // Chooses the EGL config to set element size for RGB, Alpha (opacity), depth and stencil.
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);
        gvrView.setRenderer(this);
        // Used to suggest the user to put the phone into a Cardboard viewer.
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

    /**
     * Handles the app behaviour when the back button is pressed.
     * In particular, removes the handler's callbacks and stops all the active timers.
     * Also, releases resources calling {@code shutdown()}.
     */
    @Override
    public void onBackPressed() {
        mHandler.removeCallbacksAndMessages(null);
        if (mLevel.getLevelNumber() > 2)
            for (int i = 0; i < TARGET_NUMBER; i++) {
                mPickableTargets[i].getTimer().stopAndHide();
            }
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_view);
        gvrView.shutdown();
        super.onBackPressed();
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

        int objectPositionParam = GLES20.glGetAttribLocation(objectProgram, "a_Position");
        int objectUvParam = GLES20.glGetAttribLocation(objectProgram, "a_UV");

        // Returns the location of the uniform variable u_MVP within the program 'objectProgram'.
        objectModelViewProjectionParam = GLES20.glGetUniformLocation(objectProgram, "u_MVP");

        roomPosition.setPosition(0, DEFAULT_FLOOR_HEIGHT, 0);

        // Avoids any delays during start-up due to decoding of sound files.
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        // Starts spatial audio playback of OBJECT_SOUND_FILE, according to the
                        // model's position. The returned sourceId handle is stored and allows for
                        // repositioning the sound object whenever the target position changes.
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

        // Initializes all necessary objects.
        try {
            Target room = new Target(ObjName.ROOM, getString(R.string.room_obj), getString(R.string.room_png), getString(R.string.room_png));
            mTargetManager.applyTexture(this, room, objectPositionParam, objectUvParam);
            roomTextureMesh = mTargetManager.getTexturedMesh();
            roomTexture = mTargetManager.getSelectedTexture();
            addTargets(objectPositionParam, objectUvParam);
        } catch (IOException e) {
            Log.e(TAG, getString(R.string.init_failed), e);
        }
        // Chooses randomly the first mesh to show for each pickable object.
        for (int i = 0; i < TARGET_NUMBER; i++) {
            mPickableTargets[i].setMeshIndex(random.nextInt(TARGET_MESH_COUNT));
            mPickableTargets[i].setTarget(mTargets.get(mPickableTargets[i].getMeshIndex()));
        }
        // Manages the transition to the next level.
        changeLevel();
    }

    /**
     * Updates the sounds' positions in order to have a correspondence with the objects' positions.
     * This is fundamental to keep a spatial audio.
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

        // Translates the headView matrix by the vector (0, 0, -eyeZ).
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

        // Builds the ModelView and ModelViewProjection matrices for calculating the position of the target object.
        // Then, the target is drawn on the scene.
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
        // Draws the objects on the scene if their timer are not finished and the game is not over.
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
     * The method checks all the objects on the scene and hides the one the user is looking at.
     * If the player picks the wrong object, he will lose a life and if all lives are lost
     * the game will over.
     * Otherwise, the score will increase.
     */
    @Override
    public void onCardboardTrigger() {
        for (int i = 0; i < TARGET_NUMBER; i++)
            if (isLookingAtTarget(mPickableTargets[i])) {
                if (checkCategory(mPickableTargets[i].getTarget().getCategory())) {
                    gameStatus.increaseScore(mPickableTargets[i].getTarget().getScore());
                } else {
                    gameStatus.decreaseLives(1);
                    if (gameStatus.isGameOver()) {
                        gameOver = true;
                        gameEnd();
                        break;
                    }
                }
                // Displays current score and remaining lives.
                showStatus();
                successSourceId = gvrAudioEngine.createStereoSound(SUCCESS_SOUND_FILE);
                gvrAudioEngine.playSound(successSourceId, false /* looping disabled */);

                hideTarget(mPickableTargets[i]);
                checkMesh(mPickableTargets[i]);
                break;
            }
    }

    /**
     * Performs the game over procedure, hiding all the object and stopping their timer,
     * and restarts the app after {@value TIME_BEFORE_RESTART} seconds.
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
     * Shows on the screen the final score and the player's personal record.
     * If the user has completed all levels, TIME IS OVER!! will be displayed.
     * On the contrary, if the player has lost all lives, GAME OVER!! will be shown.
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
     * Handles the change of level after a fixed amount of time (which is the level duration).
     * Since there are 3 different levels, when the duration time of the first one is reached, there is
     * a switch to the second one. Same thing for the third level.
     * In each transition, a random request for the following level is chosen and shown to the player.
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
                        // The game finishes after a fixed time (otherwise it would be infinite).
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
     * Firstly, it generates a new random position, then checks the Euclidean distance between
     * the new position and the position of all objects on the scene.
     * The distance must be of at least 2.0 (empirical value) in order to avoid overlaps
     * between objects.
     * If the new position does not respect the minimum distance, a new one will be calculated.
     *
     * @return The new {@link Position}.
     */
    private Position newPosition() {
        float distance;

        Position tempPosition = new Position();
        tempPosition.generateRandomPosition();

        float tempCoordinates[] = tempPosition.getPosition(); // [x, y, z]

        for (int i = 0; i < TARGET_NUMBER; i++) {
            float targetPosition[] = mPickableTargets[i].getPositionAsArray(); // [x, y, z]

            // Calculates the Euclidean distance between the new position and all the current objects.
            distance = (float) Math.sqrt(Math.pow((tempCoordinates[0] - targetPosition[0]), 2) +
                    Math.pow((tempCoordinates[1] - targetPosition[1]), 2) +
                    Math.pow((tempCoordinates[2] - targetPosition[2]), 2));

            // If the distance is <2.0, calculates a new random position and restarts the loop.
            if (distance < 2.0) {
                tempPosition.generateRandomPosition();
                tempCoordinates = tempPosition.getPosition();

                i = 0;
            }
        }
        return tempPosition;
    }

    /**
     * Controls if the mesh of the {@link PickableTarget} object passed belongs to the category
     * specified by the current level. If there are no objects on the scene matching the level category,
     * a new mesh will be calculated in order to have at least one target of the right type.
     *
     * @param pickableTarget The {@link PickableTarget} object to control.
     */
    private void checkMesh(PickableTarget pickableTarget) {
        // If true, the object passed as param belongs to the same category of the one requested by the level.
        if (checkCategory(pickableTarget.getTarget().getCategory()))
            return;

        // Controls if there is at least one object of the right category.
        for (int i = 0; i < TARGET_NUMBER; i++) {
            if (checkCategory(mPickableTargets[i].getTarget().getCategory()))
                return;
        }

        int newMesh;
        // Changes the mesh of the object with a new one, until it matches the level's category.
        do {
            newMesh = random.nextInt(TARGET_MESH_COUNT);
        } while (!checkCategory(mTargets.get(newMesh).getCategory()));

        // Updates the pickableTarget object with the new mesh.
        pickableTarget.changeMesh(newMesh, mTargets.get(newMesh));
    }

    /**
     * Checks if user is looking at the target object by calculating where the object is in eye-space.
     *
     * @return True if the user is looking at the target object.
     */
    private boolean isLookingAtTarget(PickableTarget pickableTarget) {
        // Converts object space to camera space. Uses the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, pickableTarget.getPosition().getModel(), 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);

        float angle = Util.angleBetweenVectors(tempPosition, FORWARD_VEC);
        return angle < ANGLE_LIMIT;
    }

    /**
     * Checks if the category passed as param is the same of the level's request.
     *
     * @param obj The category of the {@link PickableTarget} to check.
     * @return True if the categories match, false otherwise.
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

        mTargets.add(new Target(ObjName.PENGUIN, getString(R.string.penguin_obj), getString(R.string.dark_penguin), getString(R.string.light_penguin)));
        mTargets.add(new Target(ObjName.CAT, getString(R.string.cat_obj), getString(R.string.dark_cat), getString(R.string.light_cat)));
        mTargets.add(new Target(ObjName.PIKACHU, getString(R.string.pikachu_obj), getString(R.string.dark_pikachu), getString(R.string.light_pikachu)));
        mTargets.add(new Target(ObjName.GREEN_ANDROID, getString(R.string.bot_obj), getString(R.string.dark_bot), getString(R.string.light_bot)));
        mTargets.add(new Target(ObjName.CACTUS, getString(R.string.cactus_obj), getString(R.string.dark_cactus), getString(R.string.light_cactus)));
        mTargets.add(new Target(ObjName.MOUSE, getString(R.string.mouse_obj), getString(R.string.dark_mouse), getString(R.string.light_mouse)));
        mTargets.add(new Target(ObjName.PLANE, getString(R.string.plane_obj), getString(R.string.dark_plane), getString(R.string.light_plane)));
        mTargets.add(new Target(ObjName.SUNFLOWER, getString(R.string.sun_obj), getString(R.string.dark_sun), getString(R.string.light_sun)));

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
