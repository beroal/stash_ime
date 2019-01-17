package ua.in.beroal.stash_ime;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.GridLayout;

import ua.in.beroal.android.ClickPoint;

public class ClickPointGridLayout extends GridLayout {
    private ClickPoint clickPoint;

    public ClickPointGridLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        clickPoint = new ClickPoint(this);
    }

    public ClickPointGridLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClickPointGridLayout(Context context) {
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
