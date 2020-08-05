package com.esp1920.lookandpick;

import android.os.CountDownTimer;
import android.os.Handler;

import java.util.Random;

/**
 * This class manages game levels. It allows to set the object's category to collect.
 * Each level has a timer.
 */
public class Level {
    // TODO: e se nessun oggetto di quella categoria compare?
    private final String TAG = "Level";
    private static int levelNumber = 0;

    private ObjCategory mCategory;

    private Random random;

    //TODO: gestione di un booleano che indica se il timer è presente o meno nel livello levelNumber
    // e di un booleano che indichi se gli oggetti vanno gestiti con o senza categoria associata

    // private Handler mHandler;

    // TODO: come gestire il timer?
    // usare lo stesso di DisappearanceTimer oppure uno proprio?
    // private DisappearanceTimer mTimer;


    Level(){
        random = new Random();
        setCategory(randomCategory());

        // mTimer = new DisappearanceTimer(30000);
        // mTimer.startTimer();

    }


    // TODO: non saprei come farlo...
    private ObjCategory randomCategory() {
        switch(random.nextInt(4)){
            case 0:
                return ObjCategory.ANDROID_BOT;
            case 1:
                return ObjCategory.ANIMAL;
            case 2:
                return ObjCategory.BONUS;
            case 3:
                return ObjCategory.PLANT;
        }
        return null;
    }

    public static int getLevelNumber() {
        return levelNumber;
    }

    public ObjCategory getCategory() {
        return mCategory;
    }

    public void setCategory(ObjCategory category) {
        mCategory = category;
    }

   // public boolean isFinished() {
   //     return mTimer.timeFinished();
   // }

   // public void newLevelTimer() {
   //     mTimer.restartTimer();
   //     levelNumber++;
   // }

    public void nextLevel() {
        levelNumber++;
    }
}
