package com.esp1920.lookandpick;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

/**
 * Contains two sub-views to provide a simple stereo HUD.
 */
public class VrTextView extends LinearLayout {

    private final VrEyeTextView mLeftEyeView;
    private final VrEyeTextView mRightEyeView;

    private AlphaAnimation longFading;
    private AlphaAnimation shortFading;

    private static final long TOAST_LONG = 20000;
    private static final long TOAST_SHORT = 5000;

    private static final float OFFSET_LEFT = 0.015f;
    private static final float OFFSET_RIGHT = -0.050f;


    public VrTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);

        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(0, 0, 0, 0);

        mLeftEyeView = new VrEyeTextView(context, attrs);
        mLeftEyeView.setLayoutParams(params);
        addView(mLeftEyeView);

        mRightEyeView = new VrEyeTextView(context, attrs);
        mRightEyeView.setLayoutParams(params);
        addView(mRightEyeView);

        setLeftOffset(OFFSET_LEFT);
        setRightOffset(OFFSET_RIGHT);

        setColor(Color.rgb(54, 54, 54));

        setVisibility(View.VISIBLE);

        // Fading effect to fully opaque (1.0f) to fully transparent (0.0f)
        longFading = new AlphaAnimation(1.0f, 0.0f);
        longFading.setDuration(TOAST_LONG);
        shortFading = new AlphaAnimation(1.0f, 0.0f);
        shortFading.setDuration(TOAST_SHORT);
    }

    public void showLongToast(String text) {
        setText(text);
        setTextAlpha(1.0f);
        longFading.setAnimationListener(new EndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setTextAlpha(0.0f);
            }
        });
        startAnimation(longFading);
    }

    public void showShortToast(String text) {
        setText(text);
        setTextAlpha(1.0f);
        shortFading.setAnimationListener(new EndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                setTextAlpha(0.0f);
            }
        });
        startAnimation(shortFading);
    }

    public void showText(String text){
        setText(text);
    }

    private abstract class EndAnimationListener implements Animation.AnimationListener {
        @Override public void onAnimationRepeat(Animation animation) {}
        @Override public void onAnimationStart(Animation animation) {}
    }

    private void setLeftOffset(float offset) {
        mLeftEyeView.setOffset(offset);
    }

    private void setRightOffset(float offset) {
        mRightEyeView.setOffset(offset);
    }

    private void setText(String text) {
        mLeftEyeView.setText(text);
        mRightEyeView.setText(text);
    }

    private void setTextAlpha(float alpha) {
        mLeftEyeView.setTextViewAlpha(alpha);
        mRightEyeView.setTextViewAlpha(alpha);
    }

    private void setColor(int color) {
        mLeftEyeView.setColor(color);
        mRightEyeView.setColor(color);
    }
}

