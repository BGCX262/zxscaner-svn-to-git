package jp.co.laurus.android.zxscaner;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import static jp.co.laurus.android.zxscaner.MainActivity.TAG;

public final class Preview extends SurfaceView implements SurfaceHolder.Callback {
	private static final int TRY_PREVIEW = 15;
	
	private MainActivity mMain;
	private Camera mCamera;
	private FinderView mFinder;
	private SurfaceHolder mHolder;
	private Rect mFramingRect;
	private Point mResolution;
	private Decoder mDecoder;
	private ResultDialog mResultDialog;
	private int previewCount;
	private boolean isPreviewing = false;
	
	public Preview(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, "new Preview");
		
		mDecoder = new Decoder();
		mResultDialog = new ResultDialog(context);
		mMain = (MainActivity) context;
		
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void setFinder(FinderView finder) {
		mFinder = finder;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceChanged / w:" + width + ", h:" + height);
		
		if (mCamera != null) {
			if (isPreviewing) {
				mCamera.stopPreview();
			}
			
			Camera.Parameters params = mCamera.getParameters();
			params.setPreviewSize(width, height);
			mCamera.setParameters(params);
			try {
				mCamera.setPreviewDisplay(holder);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			mCamera.startPreview();
			isPreviewing = true;
			requestPreview();
		}
		
		refreshScreenResolution();
		refreshFrameRect();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		mCamera = Camera.open();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		if (mCamera != null && isPreviewing) {
			isPreviewing = false;
			closeCamera();
		}
	}
	
	public void closeCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
	
	private void refreshFrameRect() {
		if (mFinder != null) {
			mFinder.setFramingRect(getFrameRect());
		}
	}
	
	private Rect getFrameRect() {
		Log.i(TAG, "getFrameRect");
		if (mFramingRect == null) {
			int size = ((mResolution.x < mResolution.y) ? mResolution.x: mResolution.y) * 3 / 4;
			int leftOffset = (mResolution.x - size) / 2;
			int topOffset = (mResolution.y - size) / 2;
			mFramingRect = new Rect(leftOffset, topOffset, leftOffset + size, topOffset + size);
		}
		return mFramingRect;
	}
	
	private void refreshScreenResolution() {
		int w = getWidth();
		int h = getHeight();
		Log.i(TAG, "refreshScreenResolution(w: " + w + ", h:" + h);
		mResolution = new Point(w, h);
	}
	
	void requestAutoFocus() {
		Log.i(TAG, "requestAutoFocus");
		if (mCamera != null) {
			mCamera.autoFocus(autoFocusCallback);
		}
	}
	
	private void requestPreview() {
		Log.i(TAG, "requestPreview");
		if (mCamera != null) {
			mCamera.setPreviewCallback(previewCallback);
		}
	}
	
	private final Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
		
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Log.i(TAG, "onPreviewFrame");
			camera.setPreviewCallback(null);
			Result result = mDecoder.decode(data, mResolution.x, mResolution.y, getFrameRect());
			if (result != null) {
				previewCount = 0;
				BarcodeFormat code = result.getBarcodeFormat();
				Log.i(TAG, "preview success(" + code + ", " + result.getText() + ")");
				
				if (code == BarcodeFormat.EAN_13) {
					Bitmap img = mDecoder.getResultBitmap0();
					mResultDialog.set(img, result);
					mResultDialog.show();
				} else {
					mMain.showIncorrectDialog(code.toString());
				}
			} else {
				Log.i(TAG, "preview failed");
				if (previewCount < TRY_PREVIEW) {
					Log.i(TAG, "retry preview");
					previewCount++;
					requestPreview();
				} else {
					Log.i(TAG, "give up preview");
					previewCount = 0;
					requestAutoFocus();
				}
			}
		}
	};

	private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
		
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			Log.i(TAG, "onAutoFocus");
			camera.autoFocus(null);
			requestPreview();
		}
	};
}
