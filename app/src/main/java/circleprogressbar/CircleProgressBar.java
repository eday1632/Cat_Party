/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package circleprogressbar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import www.appawareinc.org.catparty.R;

public class CircleProgressBar extends ImageView {

    // PX
    private static final int DEFAULT_CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private static final int DEFAULT_CIRCLE_DIAMETER = 56;
    private static final int STROKE_WIDTH_LARGE = 3;

    private int mBackGroundColor;
    private int mProgressStokeWidth;
    private int mInnerRadius;
    private ShapeDrawable mBgCircle;
    private MaterialProgressDrawable mProgressDrawable;
    private boolean mCircleBackgroundEnabled;

    public CircleProgressBar(Context context) {
        super(context);
        init(context, null, 0);
        mBgCircle = new ShapeDrawable();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
        mBgCircle = new ShapeDrawable();
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
        mBgCircle = new ShapeDrawable();
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CircleProgressBar, defStyleAttr, 0);
        final float density = getContext().getResources().getDisplayMetrics().density;

        mBackGroundColor = a.getColor(
                R.styleable.CircleProgressBar_mlpb_background_color, DEFAULT_CIRCLE_BG_LIGHT);

        mInnerRadius = a.getDimensionPixelOffset(
                R.styleable.CircleProgressBar_mlpb_inner_radius, -1);

        mProgressStokeWidth = a.getDimensionPixelOffset(
                R.styleable.CircleProgressBar_mlpb_progress_stoke_width, (int) (STROKE_WIDTH_LARGE * density));

        mCircleBackgroundEnabled = a.getBoolean(R.styleable.CircleProgressBar_mlpb_enable_circle_background, true);

        a.recycle();
        mProgressDrawable = new MaterialProgressDrawable(getContext(), this);
        super.setImageDrawable(mProgressDrawable);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        final float density = getContext().getResources().getDisplayMetrics().density;
        int mDiameter = Math.min(getMeasuredWidth(), getMeasuredHeight());
        if (mDiameter <= 0) {
            mDiameter = (int) density * DEFAULT_CIRCLE_DIAMETER;
        }
        if (getBackground() == null && mCircleBackgroundEnabled) {

            ViewCompat.setLayerType(this, ViewCompat.LAYER_TYPE_SOFTWARE, mBgCircle.getPaint());

            mBgCircle.getPaint().setColor(mBackGroundColor);
            setBackgroundDrawable(mBgCircle);
        }
        mProgressDrawable.setBackgroundColor(mBackGroundColor);
        mProgressDrawable.setSizeParameters(mDiameter, mDiameter,
                mInnerRadius <= 0 ? (mDiameter - mProgressStokeWidth * 2) / 4 : mInnerRadius,
                mProgressStokeWidth, mProgressStokeWidth * 4, mProgressStokeWidth * 2);

        super.setImageDrawable(null);
        super.setImageDrawable(mProgressDrawable);
        mProgressDrawable.setAlpha(255);
        mProgressDrawable.start();
    }

    @Override
    final public void setImageURI(Uri uri) {
        super.setImageURI(uri);
    }

    /**
     * Update the background color of the mBgCircle image view.
     */
    public void setBackgroundColor(int colorRes) {
        if (getBackground() instanceof ShapeDrawable) {
            final Resources res = getResources();
            ((ShapeDrawable) getBackground()).getPaint().setColor(res.getColor(colorRes));
        }
    }

    @Override
    public int getVisibility() {
        return super.getVisibility();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mProgressDrawable != null) {
            if (visibility != VISIBLE) {
                mProgressDrawable.stop();
            } else {
                mProgressDrawable.start();
                mProgressDrawable.setVisible(visibility == VISIBLE, false);
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mProgressDrawable != null) {
            mProgressDrawable.stop();
            mProgressDrawable.setVisible(getVisibility() == VISIBLE, false);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mProgressDrawable != null) {
            mProgressDrawable.stop();
            mProgressDrawable.setVisible(false, false);
        }
    }
}
