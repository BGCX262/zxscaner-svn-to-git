package jp.co.laurus.android.zxscaner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public final class MainActivity extends Activity {
	final public static String TAG = "ZXScaner";
	private static final int INCORRECT_BARCODE_TYPE = 0;

	private FinderView mFinderView;
	private Preview mPreview;
	private String mBarcodeType;
	private String mIncorrectBarcodeError;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// 画面を明るい状態に保つ
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.main);

		// プレビュー用のファインダを表示する
		mFinderView = (FinderView) findViewById(R.id.viewfinder_view);
		mPreview = (Preview) findViewById(R.id.preview_surface);
		mPreview.setFinder(mFinderView);

		mIncorrectBarcodeError = getResources().getString(
				R.id.incorrect_barcode_error);
	}

	void requestAutoFocus() {
		mPreview.requestAutoFocus();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mPreview.closeCamera();
	}

	void showIncorrectDialog(String barcodeType) {
		mBarcodeType = barcodeType;
		showDialog(INCORRECT_BARCODE_TYPE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case INCORRECT_BARCODE_TYPE:
			return new AlertDialog.Builder(this).setTitle(
					mBarcodeType + " : " + mIncorrectBarcodeError)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mPreview.requestAutoFocus();
								}
							}).setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mPreview.requestAutoFocus();
								}
							}).create();
		}
		return super.onCreateDialog(id);
	}
}