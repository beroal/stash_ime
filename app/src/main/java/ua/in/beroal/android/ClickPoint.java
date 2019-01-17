package ua.in.beroal.android;

import android.view.MotionEvent;
import android.view.View;

public class ClickPoint {
    private float initialDownX;
    private float initialDownY;
    private boolean drag;
    private OnClickPointListener onClickPointListener;
    private OnDragFromListener onDragFromListener;
    private View parent;

    public ClickPoint(View parent) {
        this.parent = parent;
    }

    public void setOnClickPointListener(OnClickPointListener onClickPointListener) {
        this.onClickPointListener = onClickPointListener;
    }

    public void setOnDragFromListener(OnDragFromListener onDragFromListener) {
        this.onDragFromListener = onDragFromListener;
    }

    public interface OnClickPointListener {
        void onClickPoint(View v, float x, float y);
    }

    public interface OnDragFromListener {
        void onDragFrom(View v);
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
        boolean newDrag = !(Math.abs(x - initialDownX) < 10 && Math.abs(y - initialDownY) < 10);
        if (newDrag && !drag && onDragFromListener != null) {
            onDragFromListener.onDragFrom(parent);
        }
        drag = drag || newDrag;
    }
}
