package jp.co.laurus.android.zxscaner;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.zxing.common.BaseMonochromeBitmapSource;

public class YUVMonochromeBitmapSource extends BaseMonochromeBitmapSource {

	private final byte[] mYUVData;
	private final int mDataWidth;
	private final Rect mCrop;
	
	YUVMonochromeBitmapSource(byte[] yuvData, int width, int height, Rect crop) {
		super(width, height);
		
		mYUVData = yuvData;
		mDataWidth = width;
		mCrop = crop;
		assert(crop.width() <= width);
		assert(crop.height() <= height);
	}
/*	
	@Override
	public int getHeight() {
		return mCrop.height();
	}
	
	@Override
	public int getWidth() {
		return mCrop.width();
	}
*/
	@Override
	protected int getLuminance(int x, int y) {
		return mYUVData[(y + mCrop.top) + mDataWidth + x + mCrop.left] & 0xff;
	}

	@Override
	protected int[] getLuminanceColumn(int x, int[] column) {
		int height = getHeight();
		if (column == null || column.length < height) {
			column = new int[height];
		}
		int offset = mCrop.top + mDataWidth + mCrop.left + x;
		for (int y = 0; y < height; y++) {
			column[y] = mYUVData[offset] & 0xff;
			offset += mDataWidth;
		}
		return column;
	}

	@Override
	protected int[] getLuminanceRow(int y, int[] row) {
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new int[width];
		}
		int offset = (y + mCrop.top) * mDataWidth + mCrop.left;
		for (int x = 0; x < width; x++) {
			row[x] = mYUVData[offset + x] & 0xff;
		}
		return row;
	}

	public Bitmap renderToBitmap() {
		int width = mCrop.width();
		int height = mCrop.height();
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			int base = (y + mCrop.top) * mDataWidth +  mCrop.left;
			for (int x = 0; x < width; x++) {
				int gray = mYUVData[base + x] & 0xff;
				pixels[y * width + x] = (0xff << 24) | (gray << 16) | (gray << 8) | gray;
			}
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
}
