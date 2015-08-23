package jp.co.laurus.android.zxscaner;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;

import static jp.co.laurus.android.zxscaner.MainActivity.TAG;

public final class Decoder {
	private final MultiFormatReader mMultiFormatReader;
	private Bitmap mResultBitmap;

	Decoder() {
		Log.i(TAG, "new Decoder");
		mMultiFormatReader = new MultiFormatReader();
	}

	public Bitmap getResultBitmap0() {
		return mResultBitmap;
	}

	Result decode(byte[] data, int width, int height, Rect frame) {
		Log.i(TAG, "decode");

		if (data == null) {
			return null;
		}

		Result rawResult = null;
		YUVMonochromeBitmapSource source = new YUVMonochromeBitmapSource(data,
				width, height, frame);
		if (source != null) {
			try {
				rawResult = mMultiFormatReader.decodeWithState(source);
				mResultBitmap = source.renderToBitmap();
			} catch (Exception e) {
				// ignore
			}
		}

		return rawResult;
	}
}
