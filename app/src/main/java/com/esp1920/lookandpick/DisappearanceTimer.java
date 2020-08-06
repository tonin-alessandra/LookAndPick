package com.esp1920.lookandpick;

import android.os.CountDownTimer;
import android.util.Log;

/**
 * This class represents a timer after which an object disappears from the scene and can no longer be picked.
 */
public class DisappearanceTimer {
    private static final String TAG = "DisappearanceTimer";

    //This indicates the time an object can remain in the scene before disappearing.
    private final static long TIMER = 20000;
    private final static long INTERVAL = 1000;

    private CountDownTimer mCDTimer;
    private boolean mHidden;

    /**
     * Constructor. It initializes the timer with a default duration.
     */
    public DisappearanceTimer() {
        mHidden = false;
        mCDTimer = new CountDownTimer(TIMER, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // This does nothing for us
                // Log.d(TAG, "*******tempo rimanente:********" + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                //Calls method to hide the object
                mHidden = true;
                Log.d(TAG, "*******timer finito**********");

            }
        };
    }


    /**
     * Constructor.
     *
     * @param duration The timer duration expressed in milliseconds.
     */
    public DisappearanceTimer(long duration) {
        mHidden = false;
        mCDTimer = new CountDownTimer(duration, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // This does nothing for us
                // Log.d(TAG, "*******tempo rimanente:********" + millisUntilFinished);
            }

            @Override
            public void onFinish() {
                //Calls method to hide the object
                mHidden = true;
                Log.d(TAG, "*******timer finito**********");

            }
        };
    }

    /**
     * Starts the countdown for this object.
     */
    public void startTimer() {
        if (mCDTimer != null) {
            mCDTimer.start();
            Log.d(TAG, "*******timer partito**********");
        }
    }

    /**
     * Restarts the countdown for this object.
     */
    public void restartTimer() {
        Log.d(TAG, "*******restart timer***********");
        stopTimer();
        startTimer();
    }

    /**
     * Cancels the countdown for this object.
     */
    public void stopTimer() {
        if (mCDTimer != null) {
            mCDTimer.cancel();
            mHidden = false;
            Log.d(TAG, "*******timer stoppato***********");
        }
    }

    /**
     * Gets the {@link CountDownTimer} object.
     */
    private CountDownTimer getCDTimer() {
        return mCDTimer;
    }

    /**
     * Checks if the timer for this object is finished.
     */
    public boolean timeFinished() {
        return mHidden;
    }
}
