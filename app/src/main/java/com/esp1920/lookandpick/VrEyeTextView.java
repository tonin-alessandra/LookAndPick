package com.esp1920.lookandpick;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple view group containing some horizontally centered text underneath a horizontally
 * centered image.
 *
 * This is a helper class for VrTextView.
 */
public class VrEyeTextView extends ViewGroup {

    private final TextView textView;

    // This is necessary as the TextViews for the eyes must be correctly aligned in order to
    // be seen correctly in VR
    private float offset;

    // Vertical position of the text (as fraction of the ViewGroup's height)
    // In this case, text will be centered in height
    private static final float VERTICAL_TEXT_POS = 0.50f;

    public VrEyeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        textView = new TextView(context, attrs);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.0f);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);
        addView(textView);
    }

    public void setColor(int color) {
        textView.setTextColor(color);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public void setTextViewAlpha(float alpha) {
        textView.setAlpha(alpha);
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // Sets TextView layout
        final int tvWidth = right - left;
        final int tvHeight = bottom - top;

        float tvLeft = offset * tvWidth;
        float tvRight = tvLeft + tvWidth;

        float tvTop = tvHeight * VERTICAL_TEXT_POS;
        float tvBottom = tvTop * 2; // tvBottom = tvTop + tvHeight * (1 - VERTICAL_TEXT_POS))

        textView.layout((int) tvLeft, (int) tvTop, (int) (tvRight), (int) (tvBottom));

    }
}