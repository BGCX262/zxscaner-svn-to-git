package jp.co.laurus.android.zxscaner;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import static jp.co.laurus.android.zxscaner.MainActivity.TAG;

public final class FinderView extends View {
	private static final int[] LASER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
	
	private static final int ANIMATION_DELAY = 100;
	
	private final Paint mPaint;
	private final Rect mBox;
	private Rect mFrame;
	private final int mMaskColor;
	private final int mResultColor;
	private final int mFrameColor;
	private final int mLaserColor;
	private int mLaserIndex;
	
	public FinderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, "new FinderView");
		
		mPaint = new Paint();
		mBox = new Rect();
		
		Resources resources = getResources();
		mMaskColor = resources.getColor(R.color.finder_mask);
		mResultColor = resources.getColor(R.color.result_mask);
		mFrameColor = resources.getColor(R.color.finder_frame);
		mLaserColor = resources.getColor(R.color.finder_laser);
		mLaserIndex = 0;
	}
	
	void setFramingRect(Rect frame) {
		mFrame = frame;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		if (mFrame == null) {
			return;
		}
		
		Log.i(TAG, "onDraw");
		
		Rect frame = mFrame;
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		mPaint.setColor(mMaskColor);
		mBox.set(0, 0, width, frame.top);
		canvas.drawRect(mBox, mPaint);
		mBox.set(0, frame.top, frame.left, frame.bottom + 1);
		canvas.drawRect(mBox, mPaint);
		mBox.set(frame.right + 1, frame.top, width, frame.bottom + 1);
		canvas.drawRect(mBox, mPaint);
		mBox.set(0, frame.bottom + 1, width, height);
		canvas.drawRect(mBox, mPaint);
		
		mPaint.setColor(mFrameColor);
		mBox.set(frame.left, frame.top, frame.right + 1, frame.top + 2);
		canvas.drawRect(mBox, mPaint);
		mBox.set(frame.left, frame.top + 2, frame.left + 2, frame.bottom - 1);
		canvas.drawRect(mBox, mPaint);
		mBox.set(frame.right - 1, frame.top, frame.right + 1, frame.bottom - 1);
		canvas.drawRect(mBox, mPaint);
		mBox.set(frame.left, frame.bottom - 1, frame.right + 1, frame.bottom + 1);
		canvas.drawRect(mBox, mPaint);
		
		mPaint.setColor(mLaserColor);
		mPaint.setAlpha(LASER_ALPHA[mLaserIndex]);
		mLaserIndex = (mLaserIndex + 1) % LASER_ALPHA.length;
		
		int middle = frame.height() / 2 + frame.top;
		int left = frame.left + 2;
		int right = frame.right - 2;
		mBox.set(left, middle - 1, right, middle + 2);
		canvas.drawRect(mBox, mPaint);
		
		postInvalidateDelayed(ANIMATION_DELAY, mBox.left, mBox.top, mBox.right, mBox.bottom);
	}
}
