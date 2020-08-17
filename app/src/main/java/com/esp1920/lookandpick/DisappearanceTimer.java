package com.esp1920.lookandpick;

import android.os.CountDownTimer;

/**
 * This class represents a timer after which an object disappears from the scene and can no longer be picked.
 */
public class DisappearanceTimer {
    private static final String TAG = "DisappearanceTimer";

    // This indicates the time an object can remain in the scene before disappearing.
    private long timer;
    private final static long INTERVAL = 1000;

    private CountDownTimer mCDTimer;
    private boolean mHidden;

    /**
     * Constructor.
     *
     * @param duration The timer duration expressed in milliseconds.
     */
    public DisappearanceTimer(long duration) {
        mHidden = false;
        timer = duration;
        mCDTimer = new CountDownTimer(timer, INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // This does nothing for us.
            }

            @Override
            public void onFinish() {
                // When time is finished, the object must disappear.
                mHidden = true;
            }
        };
    }

    /**
     * Starts the countdown for this object.
     */
    private void startTimer() {
        if (mCDTimer != null) {
            mCDTimer.start();
        }
    }

    /**
     * Restarts the countdown for this object.
     */
    public void restartTimer() {
        stopTimer();
        startTimer();
    }

    /**
     * Cancels the countdown for this object.
     */
    private void stopTimer() {
        if (mCDTimer != null) {
            mCDTimer.cancel();
            mHidden = false;
        }
    }

    /**
     * Stops the timer and hides this object. For example, it is useful when the game is over and
     * all objects have to disappear, without continuing the timers' countdown.
     */
    public void stopAndHide() {
        stopTimer();
        mHidden = true;
    }

    /**
     * Checks if the timer for this object is finished.
     */
    public boolean timeFinished() {
        return mHidden;
    }
}
