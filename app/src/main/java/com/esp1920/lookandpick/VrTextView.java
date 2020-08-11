package com.esp1920.lookandpick;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

/**
 * This class implements a simple layout to show text in a GvrView.
 * It contains two TextViews, one for each eye, and extends LinearLayout to align them horizontally.
 */
public class VrTextView extends LinearLayout {

    // A VrTextView contains two TextViews, one for each eye.
    private final VrEyeTextView mLeftEyeView;
    private final VrEyeTextView mRightEyeView;

    // Animations to make text fade out
    private AlphaAnimation longFading;
    private AlphaAnimation shortFading;

    private static final long TOAST_LONG = 20000;
    private static final long TOAST_SHORT = 5000;

    // Offsets that ensure that the TextView is visualized correctly while using a Cardboard
    // viewer (the left and the right TextViews need to overlap perfectly).
    // Following values have been chosen after tests, they center text in width.
    private static final float OFFSET_LEFT = 0.020f;
    private static final float OFFSET_RIGHT = -0.045f;

    /**
     * Constructor.
     * Creates a layout to show text in VR applications.
     *
     * @param context Context to show the layout.
     * @param attrs   Set of attributes associated with the tag in the xml document.
     */
    public VrTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Aligns mLeftEyeView and mRightEyeView horizontally.
        setOrientation(HORIZONTAL);

        // Layout parameters applied to each child (left and right TextViews).
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(0, 0, 0, 0);

        mLeftEyeView = new VrEyeTextView(context, attrs);
        mLeftEyeView.setLayoutParams(params);
        addView(mLeftEyeView);

        mRightEyeView = new VrEyeTextView(context, attrs);
        mRightEyeView.setLayoutParams(params);
        addView(mRightEyeView);

        // Sets offsets to make the TextViews overlap
        setLeftOffset(OFFSET_LEFT);
        setRightOffset(OFFSET_RIGHT);

        // Sets text color (this is a dark grey)
        setColor(Color.rgb(54, 54, 54));

        setVisibility(View.VISIBLE);

        // Fading effects: to fully opaque (1.0f) to fully transparent (0.0f)
        longFading = new AlphaAnimation(1.0f, 0.0f);
        longFading.setDuration(TOAST_LONG);

        shortFading = new AlphaAnimation(1.0f, 0.0f);
        shortFading.setDuration(TOAST_SHORT);
    }

    /**
     * Listener to handle notifications from an animation.
     */
    private abstract class EndAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    /**
     * Shows a string of text that will fade in TOAST_LONG milliseconds.
     *
     * @param text String of text that will be showed.
     */
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

    /**
     * Shows a string of text that will fade in TOAST_SHORT milliseconds.
     *
     * @param text String of text that will be showed.
     */
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

    /**
     * Shows a string of text.
     *
     * @param text String of text that will be showed.
     */
    // TODO: remove if never used
    public void showText(String text) {
        setText(text);
    }

    /**
     * Sets the offset for the left eye's TextView.
     *
     * @param offset Offset to set to left eye's TextView.
     */
    private void setLeftOffset(float offset) {
        mLeftEyeView.setOffset(offset);
    }

    /**
     * Sets the offset for the right eye's TextView.
     *
     * @param offset Offset to set to right eye's TextView.
     */
    private void setRightOffset(float offset) {
        mRightEyeView.setOffset(offset);
    }

    /**
     * Sets the text to write in the eyes' TextViews.
     *
     * @param text String of text that the eyes' TextViews will show.
     */
    private void setText(String text) {
        mLeftEyeView.setText(text);
        mRightEyeView.setText(text);
    }

    /**
     * Sets the alpha (opacity value) that the text in the eyes' TextViews has to have when the
     * animation starts.
     *
     * @param alpha Alpha of the text in the eyes' TextViews.
     */
    private void setTextAlpha(float alpha) {
        mLeftEyeView.setTextViewAlpha(alpha);
        mRightEyeView.setTextViewAlpha(alpha);
    }

    /**
     * Sets the color of the text in the eyes' TextViews.
     *
     * @param color Integer value of the color chosen for the text in the eyes' TextViews.
     */
    private void setColor(int color) {
        mLeftEyeView.setColor(color);
        mRightEyeView.setColor(color);
    }
}

