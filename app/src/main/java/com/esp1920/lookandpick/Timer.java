package com.esp1920.lookandpick;

import android.os.CountDownTimer;

/**
 * This class represents a timer with its related methods.
 * It will be used to make objects disappear from the scene after a fixed time.
 */
public class Timer {
    private CountDownTimer mTimer;
    private long mTimeInMillis;
    private long mTickInterval;
    private Target mObject;

    /**
     * Constructor.
     */
    public Timer(long time, long tickInterval, Target object){
        setTimer(time);
        setTickInterval(tickInterval);
        mObject = object;
        mTimer = new CountDownTimer(mTimeInMillis, mTickInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                //questo non lo useremo perchè non ci serve avere un aggiornamento ad ogni tick del timer
            }

            @Override
            public void onFinish() {
                //TODO: qua bisogna chiamare il metodo che nasconde l'oggetto
                // NB: hideObject non va bene perchè crea una nuova posizione, non lo fa solo scomparire
            }
        };
    }

    /**
     *
     */
    public void setTimer(long time){
        mTimeInMillis = time;
    }

    /**
     *
     */
    public void setTickInterval(long tickInterval){
        mTickInterval = tickInterval;
    }

}
