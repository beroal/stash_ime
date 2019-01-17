package ua.in.beroal.android;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * A {@link AppCompatTextView} with {@link ClickPoint} enhancements.
 */
public class ClickPointTextView extends AppCompatTextView {
    private ClickPoint clickPoint;

    public ClickPointTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        clickPoint = new ClickPoint(this);
    }

    public ClickPointTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickPointTextView(Context context) {
        this(context, null);
    }

    public void setOnClickPointListener(ClickPoint.OnClickPointListener listener) {
        clickPoint.setOnClickPointListener(listener);
        setClickable(true);
    }

    public void setOnDragStartedListener(ClickPoint.OnDragStartedListener listener) {
        clickPoint.setOnDragStartedListener(listener);
        setClickable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return clickPoint.onTouchEvent(isClickable(), event);
    }
}
