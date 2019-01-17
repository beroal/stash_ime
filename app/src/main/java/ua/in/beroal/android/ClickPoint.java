package ua.in.beroal.android;

import android.view.MotionEvent;
import android.view.View;

/**
 * Helps in adding a click listener that receives click point and a drag-and-drop listener
 * to a subclass of {@link View}.
 */
public class ClickPoint {
    public static final int DRAG_OFFSET_X = 10;
    public static final int DRAG_OFFSET_Y = 10;
    private float initialDownX;
    private float initialDownY;
    private boolean drag;
    private OnClickPointListener onClickPointListener;
    private OnDragStartedListener onDragStartedListener;
    private View parent;

    public ClickPoint(View parent) {
        this.parent = parent;
    }

    public void setOnClickPointListener(OnClickPointListener onClickPointListener) {
        this.onClickPointListener = onClickPointListener;
    }

    public void setOnDragStartedListener(OnDragStartedListener onDragStartedListener) {
        this.onDragStartedListener = onDragStartedListener;
    }

    public boolean onTouchEvent(boolean clickable, MotionEvent event) {
        if (!clickable) {
            return false;
        } else {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    drag = false;
                    initialDownX = event.getX();
                    initialDownY = event.getY();
                    senseDrag(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    for (int i = 0; i != event.getHistorySize(); i++) {
                        senseDrag(event.getHistoricalX(i), event.getHistoricalY(i));
                    }
                    senseDrag(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    senseDrag(event.getX(), event.getY());
                    if (!drag && onClickPointListener != null) {
                        onClickPointListener.onClickPoint(parent, event.getX(), event.getY());
                    }
                    break;
            }
            return true;
        }
    }

    private void senseDrag(float x, float y) {
        boolean newDrag = !(Math.abs(x - initialDownX) < DRAG_OFFSET_X
                && Math.abs(y - initialDownY) < DRAG_OFFSET_Y);
        if (newDrag && !drag && onDragStartedListener != null) {
            onDragStartedListener.onDragStarted(parent, initialDownX, initialDownY);
        }
        drag = drag || newDrag;
    }

    public interface OnClickPointListener {
        void onClickPoint(View v, float x, float y);
    }

    public interface OnDragStartedListener {
        /**
         * Drag started on {@code v} at the {@code (x, y)} point.
         */
        void onDragStarted(View v, float x, float y);
    }
}
