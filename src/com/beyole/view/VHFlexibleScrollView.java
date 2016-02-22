package com.beyole.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Looper;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;

public class VHFlexibleScrollView extends ScrollView {
	private static final String TAG = "FLEXIBLESCROLLVIEW";
	// ScrollView唯一的一个子view
	private View contentView;
	// 用于记录正常的布局位置
	private Rect originalRect = new Rect();
	// 记录手指按下时是否可以下拉
	private boolean canPullDown = false;
	// 记录手指按下时是否可以上拉
	private boolean canPullUp = false;
	private ViewDragHelper mViewDragHelper;

	public VHFlexibleScrollView(Context context) {
		this(context, null);
	}

	public VHFlexibleScrollView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public VHFlexibleScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	/**
	 * 在加载完xml后获取唯一的一个childview
	 */
	@Override
	protected void onFinishInflate() {
		if (getChildCount() > 0) {
			// 获取第一个childview
			contentView = getChildAt(0);
			mViewDragHelper = ViewDragHelper.create(this, 1.0f, new Callback() {

				// 控制移动的垂直范围
				@Override
				public int clampViewPositionVertical(View child, int top, int dy) {
					return top;
				}

				// 捕获的childView
				@Override
				public boolean tryCaptureView(View child, int pointerId) {
					return child == contentView;
				}

				/**
				 * view位置改变时
				 */
				@Override
				public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
					canPullDown = top > originalRect.top ? true : false;
					canPullUp = top < -1 * (originalRect.height() - getHeight()) ? true : false;
				}

				/**
				 * 手指释放时view回到初始状态
				 */
				@Override
				public void onViewReleased(View releasedChild, float xvel, float yvel) {
					if (releasedChild == contentView) {
						if (canPullDown) {
							mViewDragHelper.settleCapturedViewAt(originalRect.left, originalRect.top);
						}
						if (canPullUp) {
							mViewDragHelper.settleCapturedViewAt(originalRect.left, -(originalRect.height() - getHeight()));
						}
						invalidateView();
					}
				}

			});
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (contentView == null)
			return;
		// scrollview唯一的一个子view的位置信息，这个位置信息在整个生命周期中保持不变
		originalRect.set(contentView.getLeft(), contentView.getTop(), contentView.getRight(), contentView.getBottom());

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean shouldInterceptTouchEvent = mViewDragHelper.shouldInterceptTouchEvent(ev);
		return shouldInterceptTouchEvent;

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mViewDragHelper.processTouchEvent(ev);
		return true;
	}

	@Override
	public void computeScroll() {
		if (mViewDragHelper.continueSettling(true)) {
			invalidateView();
		}
	}

	// 重绘view
	private void invalidateView() {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			invalidate();
		} else {
			postInvalidate();
		}
	}
}
