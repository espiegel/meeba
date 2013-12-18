package com.meeba.google.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.meeba.google.R;

/**
 * This EditText has AutoComplete functionality along with ClearableEditText functionality
 */
public class AutoCompleteClearableEditText extends AutoCompleteTextView implements View.OnTouchListener,
        View.OnFocusChangeListener, TextWatcherAdapter.TextWatcherListener {

    private final int THRESHOLD = 1; // Minimum amount of chars needed to show auto-complete suggestions
    private BackEvent mBackEvent = null;

    public interface Listener {
        void didClearText();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private Drawable xD;
    private Listener listener;

    public AutoCompleteClearableEditText(Context context) {
        super(context);
        init();
    }

    public AutoCompleteClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AutoCompleteClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        this.l = l;
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener f) {
        this.f = f;
    }

    private OnTouchListener l;
    private OnFocusChangeListener f;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (getCompoundDrawables()[2] != null) {
            boolean tappedX = event.getX() > (getWidth() - getPaddingRight() - xD
                    .getIntrinsicWidth());
            if (tappedX) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(this.getWindowToken(), 0);

                    setText("");

                    if (listener != null) {
                        listener.didClearText();
                    }
                }
                return true;
            }
        }
        if (l != null) {
            return l.onTouch(v, event);
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisible(isNotEmpty(getText()));
        } else {
            setClearIconVisible(false);
        }
        if (f != null) {
            f.onFocusChange(v, hasFocus);
        }
    }

    @Override
    public void onTextChanged(EditText view, String text) {
        if (isFocused()) {
            setClearIconVisible(isNotEmpty(text));
        }
    }

    private void init() {
        setThreshold(THRESHOLD);

        xD = getCompoundDrawables()[2];
        if (xD == null) {
            xD = getResources().getDrawable(R.drawable.ic_clear_text);
        }
        xD.setBounds(0, 0, xD.getIntrinsicWidth(), xD.getIntrinsicHeight());
        setClearIconVisible(false);

        setDropDownBackgroundResource(R.drawable.bg_content_box);

        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        addTextChangedListener(new TextWatcherAdapter(this, this));
    }

    protected void setClearIconVisible(boolean visible) {
        Drawable x = visible ? xD : null;
        setCompoundDrawables(getCompoundDrawables()[0],
                getCompoundDrawables()[1], x, getCompoundDrawables()[3]);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if(mBackEvent != null) {
                mBackEvent.backPressed();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public interface BackEvent {
        public void backPressed();
    }

    public BackEvent getBackEvent() {
        return mBackEvent;
    }

    public void setBackEvent(BackEvent mBackEvent) {
        this.mBackEvent = mBackEvent;
    }

    private boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    private boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }
}
