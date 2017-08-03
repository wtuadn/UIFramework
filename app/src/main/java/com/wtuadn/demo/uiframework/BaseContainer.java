package com.wtuadn.demo.uiframework;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.wtuadn.demo.R;

/**
 * Created by wtuadn on 2017/6/23.
 */

public abstract class BaseContainer extends FrameLayout {
    private final String TAG = getClass().getSimpleName();
    protected BaseActivity mActivity;
    protected View mView;
    private int openInAnim, openOutAnim, closeInAnim, closeOutAnim;
    private int mLastVisibility = -1;
    private Bundle mState;
    private Bundle mSaveData;
    private boolean dispatchLifecycle = true;
    public boolean destroyAfterOut = false;

    private static final int width = 1080;
    private static final int edgeWidth = 30;
    private static final int duration = 200;
    private static VelocityTracker velocityTracker;
    private static float lastX;
    private static float downX;
    private static boolean dispatch;
    protected boolean canSwipeBack = true;

    /**
     * 自定义页面切换动画，需要在onCreate和onBackPressed触发前设置才生效
     */
    public BaseContainer setAnim(int openInAnim, int openOutAnim, int closeInAnim, int closeOutAnim) {
        if (openInAnim != -1) this.openInAnim = openInAnim;
        if (openOutAnim != -1) this.openOutAnim = openOutAnim;
        if (closeInAnim != -1) this.closeInAnim = closeInAnim;
        if (closeOutAnim != -1) this.closeOutAnim = closeOutAnim;
        return this;
    }

    public BaseContainer(BaseActivity context, Bundle state) {
        super(context);
        mActivity = context;
        mState = state;
        mActivity.mStack.push(this);
        //默认切换动画
        setAnim(R.anim.activity_right_in, R.anim.activity_left_out,
                R.anim.activity_left_in, R.anim.activity_right_out);
        setBackgroundColor(0xb0000000);
        setClickable(true);
    }

    public void onCreate() {
        if (mView != null) return;
        Log.d("onCreate", TAG);
        mView = onCreateView();
        addView(mView, mView.getLayoutParams() == null ? mActivity.mLayoutParams : mView.getLayoutParams());
        int size = mActivity.mStack.size();
        if (mState == null) {
            openIn();
            if (size > 1) mActivity.mStack.get(size - 2).openOut();
        } else {
            in();
            onRestore(mState);
            mState.clear();
            mState = null;
        }
    }

    public void onDestroy() {
        Log.d("onDestroy", TAG);
        mActivity.mStack.remove(this);
    }

    private void dispatchResume() {
        if (dispatchLifecycle && getVisibility() == VISIBLE) {
            if (mLastVisibility == -1) {
                mActivity.mContent.post(new Runnable() {
                    @Override
                    public void run() {
                        onResume();
                    }
                });
            } else onResume();
        }
    }

    private void dispatchPause() {
        if (dispatchLifecycle) onPause();
    }

    protected void onResume() {
        Log.d("onResume", TAG);
    }

    protected void onPause() {
        Log.d("onPause", TAG);
    }

    public boolean onBackPressed() {
        boolean handled = false;
        if (isTopContainer()) {
            if (mActivity.mStack.size() > 1) {
                BaseContainer bc = mActivity.mStack.get(mActivity.mStack.size() - 2);
                closeOut();
                handled = true;
                boolean needRestore = bc.mState != null;
                if (needRestore) bc.onCreate();
                else bc.closeIn();
            }
        } else {
            onDestroy();
            return true;
        }
        return handled;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            dispatchResume();
        } else if (visibility == INVISIBLE) {
            if (mLastVisibility != GONE) dispatchPause();
        } else {
            if (mLastVisibility != INVISIBLE) dispatchPause();
        }
        mLastVisibility = visibility;
    }

    private boolean isTopContainer() {
        return mActivity.mStack.peek() == this;
    }

    private void openIn() {
        in();
        if (mActivity.mStack.size() > 1) doAnimation(openInAnim, null);
    }

    private void closeIn() {
        in();
        doAnimation(closeInAnim, null);
    }

    private void openOut() {
        doAnimation(openOutAnim, new Runnable() {
            @Override
            public void run() {
                out();
            }
        });
    }

    private void closeOut() {
        doAnimation(closeOutAnim, new Runnable() {
            @Override
            public void run() {
                destroyAfterOut = true;
                out();
            }
        });
    }

    private void doAnimation(int animId, final Runnable callback) {
        if (animId != 0) {
            Animation animation = AnimationUtils.loadAnimation(getContext(), animId);
            if (animation != null) {
                BaseActivity.isAnimating = true;
                animation.setAnimationListener(new MyAnimationListener() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        BaseActivity.isAnimating = false;
                        if (callback != null) callback.run();
                    }
                });
                startAnimation(animation);
            } else if (callback != null) callback.run();
        } else if (callback != null) callback.run();
    }

    private void in() {
        if (getParent() == null)
            mActivity.mContent.addView(this, isTopContainer() ? -1 : 0, mActivity.mLayoutParams);
    }

    private void out() {
        dispatchPause();
        mActivity.mContent.removeView(this);
        if (destroyAfterOut) onDestroy();
    }

    public abstract View onCreateView();

    protected Bundle onSave() {
        if (mSaveData == null) mSaveData = new Bundle();
        else mSaveData.clear();
        if (mState != null) mSaveData.putAll(mState);
        return mSaveData;
    }

    protected void onRestore(Bundle state) {
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (canSwipeBack && mActivity.mStack.get(0) == this) canSwipeBack = false;
        if (!canSwipeBack) return super.dispatchTouchEvent(ev);
        createVelocityTracker(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = downX = ev.getX();
                dispatch = true;
                if (downX < edgeWidth) {
                    BaseContainer sc = mActivity.mStack.get(mActivity.mStack.size() - 2);
                    sc.dispatchLifecycle = false;
                    if (sc.mState != null) sc.onCreate();
                    else sc.in();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (downX < edgeWidth) {
                    mView.setTranslationX(ev.getX() - downX);
                    getBackground().setAlpha((int) (255 * (1 - ev.getX() / width)));
                    lastX = ev.getX();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (downX < edgeWidth) {
                    handleDragEnd();
                }
                recycleVelocityTracker();
        }
        if (downX < edgeWidth) {
            if (lastX > edgeWidth) {
                if (dispatch) {
                    super.dispatchTouchEvent(MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), MotionEvent.ACTION_CANCEL, ev.getX(), ev.getY(), ev.getMetaState()));
                    dispatch = false;
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void handleDragEnd() {
        if (!(getScrollVelocity() < -width) && (lastX > width * 0.3f || getScrollVelocity() > width)) {
            BaseActivity.isAnimating = true;
            final Drawable background = getBackground();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mView.getTranslationX(), width).setDuration(duration);
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setAnim(0, 0, 0, 0);
                    BaseContainer sc = null;
                    int[] sa = null;
                    if (mActivity.mStack.size() > 1) {
                        sc = mActivity.mStack.get(mActivity.mStack.size() - 2);
                        sa = new int[]{sc.openInAnim, sc.openOutAnim, sc.closeInAnim, sc.closeOutAnim};
                        sc.setAnim(0, 0, 0, 0);
                        sc.dispatchLifecycle = true;
                    }
                    closeOut();
                    if (sc != null) {
                        sc.dispatchResume();
                        sc.setAnim(sa[0], sa[1], sa[2], sa[3]);
                    }
                    BaseActivity.isAnimating = false;
                }
            });
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mView.setTranslationX(value);
                    background.setAlpha((int) (255 * (1 - value / width)));
                }
            });
            valueAnimator.start();
        } else {
            BaseActivity.isAnimating = true;
            mView.animate().translationX(0).setDuration(duration).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    BaseActivity.isAnimating = false;
                    BaseContainer sc = mActivity.mStack.get(mActivity.mStack.size() - 2);
                    sc.out();
                    sc.dispatchLifecycle = true;
                }
            }).start();
        }
    }

    /**
     * 创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
     */
    private void createVelocityTracker(MotionEvent event) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);
    }

    /**
     * 回收VelocityTracker对象。
     */
    private void recycleVelocityTracker() {
        velocityTracker.recycle();
        velocityTracker = null;
    }

    /**
     * 获取手指在content界面滑动的速度。
     *
     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
     */
    private int getScrollVelocity() {
        velocityTracker.computeCurrentVelocity(1000, width * 2);
        return (int) velocityTracker.getXVelocity();
    }

    private static abstract class MyAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
