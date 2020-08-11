package com.esp1920.lookandpick;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This class implements a simple ViewGroup (base class for layouts) containing a TextView.
 * This is a helper class for VrTextView.
 */
public class VrEyeTextView extends ViewGroup {

    private final TextView textView;

    // This TextView is meant to be contained by a LinearLayout (VrTextView). This is the offset
    // regarding the position of the TextView inside this layout.
    private float offset;

    // Vertical position of the text (as fraction of the ViewGroup's height)
    // In this case, text will be centered in height
    private static final float VERTICAL_TEXT_POS = 0.50f;

    /**
     * Constructor.
     * Creates a ViewGroup containing a TextView.
     *
     * @param context Context to show text.
     * @param attrs   Set of attributes associated with the tag in the xml document.
     */
    public VrEyeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        textView = new TextView(context, attrs);

        // Text formatting
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10.0f);
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
        textView.setGravity(Gravity.CENTER);

        addView(textView);
    }

    /**
     * Sets the color of the text in the TextView.
     *
     * @param color Integer value of the color chosen for the text.
     */
    public void setColor(int color) {
        textView.setTextColor(color);
    }

    /**
     * Sets the text that has to be written in the textView.
     *
     * @param text String of text that the TextView will show.
     */
    public void setText(String text) {
        textView.setText(text);
    }

    /**
     * Sets the alpha (opacity value) that the text in the TextView has to have when the
     * animation starts.
     *
     * @param alpha Alpha of the text in the TextView.
     */
    public void setTextViewAlpha(float alpha) {
        textView.setAlpha(alpha);
    }

    /**
     * Sets the offset of the TextView with respect to the outer LinearLayout.
     *
     * @param offset Offset to set to the TextView.
     */
    public void setOffset(float offset) {
        this.offset = offset;
    }

    /**
     * Assigns a size and position to each of the ViewGroup's children.
     *
     * @param changed Boolean set to true if this is a new size or position for this view.
     * @param left    Left position, relative to parent (the ViewGroup)
     * @param top     Top position, relative to parent
     * @param right   Right position, relative to parent
     * @param bottom  Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        // Calculates size and position of the TextView, with respect to the ViewGroup
        float tvWidth = right - left;
        float tvHeight = bottom - top;

        float tvLeft = offset * tvWidth;
        float tvRight = tvLeft + tvWidth;

        float tvTop = tvHeight * VERTICAL_TEXT_POS; // centered in height
        float tvBottom = tvTop + tvHeight * (1 - VERTICAL_TEXT_POS);

        // Sets size and position to the TextView
        textView.layout((int) tvLeft, (int) tvTop, (int) (tvRight), (int) (tvBottom));
    }
}