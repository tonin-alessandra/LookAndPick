package com.esp1920.lookandpick;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class manages all operations related to saving application data on ScorePreferences's sharedPreferences
 * file, in particular about the status of the game, such as score and remaining lives.
 * <p>
 * The main purpose is to save the score obtained by the player only if it is better than the
 * previous one, in order to keep track of the best score achieved.
 */
public class StatusManager {
    //This prefix is used as part of the key to save the score on sharedPreferences file.
    private final static String RECORD_KEY = "Record ";
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mEditor;
    private String mPreferencesFileName;
    //private Context mContext;
    private static StatusManager instance;

    /**
     * Constructor. It is private due to Singleton.
     *
     * @param context The current application context.
     */
    private StatusManager(Context context) {
        //mContext = context;
        mPreferencesFileName = context.getResources().getString(R.string.score_preferences);
        // Retrieves the content of the preferences file identified by mPreferencesFileName.
        // MODE_PRIVATE means that it can only be accessed by the calling application.
        mSharedPref = context.getSharedPreferences(mPreferencesFileName, Context.MODE_PRIVATE);
    }

    /**
     * Manages StatusManager object according to Singleton design pattern.
     *
     * @returns A new instance of StatusManager or the current one, if it exists.
     */
    synchronized public static StatusManager getInstance(Context context) {
        if (instance == null)
            instance = new StatusManager(context);
        return instance;
    }

    /**
     * Saves on ScorePreferences sharedPreferences file a key-value pair, only if the value is higher
     * than the saved one. This key-value pair represents a score record achieved by a player.
     * Since only one value per time will be saved, the key is a constant represented by the string
     * {@code PREFIX}.
     *
     * @param score The value of the score to be saved.
     */
    public void saveScore(int score) {
        int currentRecord = getCurrentRecord();
        if (score > currentRecord) {
            removeScore();
            mEditor = mSharedPref.edit();
            mEditor.putInt(RECORD_KEY, score);
            mEditor.apply();
        }
    }

    /**
     * Removes a value saved on sharedPreferences file. This is used to save a new record, removing
     * the previous one.
     */
    private void removeScore() {
        mEditor = mSharedPref.edit();
        mEditor.remove(RECORD_KEY);
        mEditor.apply();
    }

    /**
     * Retrieves the current score record saved on sharedPreferences file.
     *
     * @return The current record if present, -1 otherwise.
     */
    public int getCurrentRecord() {
        return mSharedPref.getInt(RECORD_KEY, -1);
    }

}
