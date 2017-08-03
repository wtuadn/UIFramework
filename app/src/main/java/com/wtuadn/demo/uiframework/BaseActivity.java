package com.wtuadn.demo.uiframework;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.FrameLayout;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * Created by wtuadn on 2017/06/24.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String SAVE_TAG = "SAVE_TAG";
    public static boolean isAnimating = false;
    protected final FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
    public final Stack<BaseContainer> mStack = new Stack<>();
    protected FrameLayout mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContent = (FrameLayout) findViewById(Window.ID_ANDROID_CONTENT);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isAnimating) return true;
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (!isAnimating && (mStack.isEmpty() || !mStack.peek().onBackPressed()))
            super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mStack.size() > 0) mStack.peek().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mStack.size() > 0) {
            Bundle bundle = new Bundle();
            for (int i = 0; i < mStack.size(); i++) {
                BaseContainer container = mStack.get(i);
                bundle.putBundle(container.getClass().getName() + " " + i, container.onSave());
            }
            outState.putBundle(SAVE_TAG, bundle);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(SAVE_TAG);
            if (bundle != null && bundle.size() > 0) {
                Set<String> keySet = bundle.keySet();
                for (int i = 0; i < bundle.size(); i++) {
                    String strI = Integer.toString(i);
                    Iterator<String> iterator = keySet.iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (key.endsWith(strI)) {
                            try {
                                Class c = Class.forName(key.split(" ")[0]);
                                Constructor constructor = c.getDeclaredConstructor(BaseActivity.class, Bundle.class);
                                constructor.setAccessible(true);
                                BaseContainer bc = (BaseContainer) constructor.newInstance(this, bundle.getBundle(key));
                                if (i == bundle.size() - 1) bc.onCreate();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
