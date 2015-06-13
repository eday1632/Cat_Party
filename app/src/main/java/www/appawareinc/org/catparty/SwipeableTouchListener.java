package www.appawareinc.org.catparty;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;


/*this class allows us to swipe a video up or down smoothly. without it, we would only be able to
 * swipe the videos left to right, which is handled automatically by the recycler view */
public class SwipeableTouchListener implements RecyclerView.OnItemTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private SnappyRecyclerView mRecyclerView;
    private LinearLayoutManager layoutManager;
    private SwipeListener mSwipeListener;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private boolean mPaused;
    private float mFinalDelta;


    public SwipeableTouchListener(SnappyRecyclerView recyclerView, SwipeListener listener) {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = recyclerView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime) + 150; //dials in the time so the layout manager can center the next view
        mRecyclerView = recyclerView;
        mSwipeListener = listener;
        layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();

        /**
         * This will ensure that this SwipeableRecyclerViewTouchListener is paused during list view scrolling.
         * If a scroll listener is already assigned, the caller should still pass scroll changes through
         * to this listener.
         */
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstPart = layoutManager.findFirstVisibleItemPosition();

                    if (firstPart > -1) {
                        int firstFull = layoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastPart = layoutManager.findLastVisibleItemPosition();
                        int lastFull = layoutManager.findLastCompletelyVisibleItemPosition();

                        ViewHolderAdapter.SimpleViewHolder FPviewHolder =
                                (ViewHolderAdapter.SimpleViewHolder)
                                        mRecyclerView.findViewHolderForPosition(firstPart);
                        ViewHolderAdapter.SimpleViewHolder FFviewHolder =
                                (ViewHolderAdapter.SimpleViewHolder)
                                        mRecyclerView.findViewHolderForPosition(firstFull);
                        ViewHolderAdapter.SimpleViewHolder LPviewHolder =
                                (ViewHolderAdapter.SimpleViewHolder)
                                        mRecyclerView.findViewHolderForPosition(lastPart);
                        ViewHolderAdapter.SimpleViewHolder LFviewHolder =
                                (ViewHolderAdapter.SimpleViewHolder)
                                        mRecyclerView.findViewHolderForPosition(lastFull);

                        if(firstFull != -1) FFviewHolder.showWebView();
                        if(lastFull != -1) LFviewHolder.showWebView();
                        if(firstFull > firstPart) FPviewHolder.hideAllViews();
                        if(lastFull < lastPart) LPviewHolder.hideAllViews();
                    }
                }
            }

        });
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        return handleTouchEvent(motionEvent);
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
    }

    private boolean handleTouchEvent(MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = mRecyclerView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    break;
                }

                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = mRecyclerView.getChildCount();
                int[] listViewCoords = new int[2];
                mRecyclerView.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = mRecyclerView.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mDownView = child;
                        break;
                    }
                }

                if (mDownView != null) {
                    mDownX = motionEvent.getRawX();
                    mDownY = motionEvent.getRawY();
                    mDownPosition = mRecyclerView.getChildPosition(mDownView);
                    if (mSwipeListener.canSwipe(mDownPosition)) {
                        mVelocityTracker = VelocityTracker.obtain();
                        mVelocityTracker.addMovement(motionEvent);
                    } else {
                        mDownView = null;
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                if (mDownView != null && mSwiping) {
                    // cancel
                    mDownView.animate()
                            .translationY(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                mFinalDelta = motionEvent.getRawY() - mDownY;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityY = mVelocityTracker.getYVelocity();
                float absVelocityY = Math.abs(velocityY);
                float absVelocityX = Math.abs(mVelocityTracker.getXVelocity());
                boolean dismiss = false;
                boolean dismissDown = false;
                if (Math.abs(mFinalDelta) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    dismissDown = mFinalDelta > 0;
                } else if (mMinFlingVelocity <= absVelocityY && absVelocityY <= mMaxFlingVelocity
                        && absVelocityX < absVelocityY && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityY < 0) == (mFinalDelta < 0);
                    dismissDown = mVelocityTracker.getYVelocity() > 0;
                }
                if (dismiss && mDownPosition != ListView.INVALID_POSITION) {
                    // dismiss
                    final View downView = mDownView; // mDownView gets null'd before animation ends
                    final int downPosition = mDownPosition;
                    mDownView.animate()
                            .translationY(dismissDown ? mViewWidth : -mViewWidth)
                            .alpha(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    performDismiss(downView, downPosition);
                                }
                            });
                } else {
                    // cancel
                    mDownView.animate()
                            .translationY(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                if (!mSwiping && Math.abs(deltaY) > mSlop && Math.abs(deltaX) < Math.abs(deltaY) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaY > 0 ? mSlop : -mSlop);
                }

                if (mSwiping) {
                    mDownView.setTranslationY(deltaY - mSwipingSlop);
                    mDownView.setAlpha(Math.max(0f, Math.min(1f,
                            1f - Math.abs(deltaY) / mViewWidth)));
                    return true;
                }
                break;
            }
        }

        return false;
    }

    private void performDismiss(final View dismissView, final int dismissPosition) {
        // Animate the dismissed list item to zero-width and fire the dismiss callback when
        // all dismissed list item animations have completed. This triggers layout on each animation
        // frame; in the future we may want to do something smarter and more performant.
        int lastVis = layoutManager.findLastVisibleItemPosition();
        int leftMargin = 0;
        int leftEdge = 0;
        int scrollDistanceLeft = 0;

        try {
            View lastView = layoutManager.findViewByPosition(lastVis);
            leftMargin = Math.round((TwoRooms.screenWidthDp - lastView.getWidth() / TwoRooms.densityMultiple) / 2);
            leftEdge = Math.round(lastView.getLeft() / TwoRooms.densityMultiple);
            scrollDistanceLeft = leftEdge - leftMargin;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        ValueAnimator animator = ValueAnimator.ofInt(scrollDistanceLeft, 1).setDuration(mAnimationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

                if (mFinalDelta > 0) {
                    mSwipeListener.onSaveBySwipeDown(mRecyclerView, dismissPosition);
                } else {
                    mSwipeListener.onShareBySwipeUp(mRecyclerView, dismissPosition);
                }

                // Reset mDownPosition to avoid MotionEvent.ACTION_UP trying to start a dismiss
                // animation with a stale position
                mDownPosition = ListView.INVALID_POSITION;

                // Send a cancel event
                long time = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                        MotionEvent.ACTION_CANCEL, 0, 0, 0);
                mRecyclerView.dispatchTouchEvent(cancelEvent);
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mRecyclerView.removeViewInLayout(dismissView);
            }
        });

        if(dismissPosition > 0) {
            if(lastVis > -1){
                mRecyclerView.fling(1, 0);
            }
        } else if (dismissPosition == 0){
            mRecyclerView.fling(-1, 0);
        }
        animator.start();
    }

    public interface SwipeListener {


        boolean canSwipe(int position);


        void onShareBySwipeUp(SnappyRecyclerView recyclerView, int shareThis);


        void onSaveBySwipeDown(SnappyRecyclerView recyclerView, int dismissThis);
    }

}