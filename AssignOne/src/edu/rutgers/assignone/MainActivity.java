package edu.rutgers.assignone;

import android.support.v7.app.ActionBarActivity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {

	TextView mWifiStatus, mDownloadProgressPercent;
	EditText et;
	String myHTTPUrl;
	CheckBox isWIFI;
	Button btnDownload;
	ProgressBar mProgressBar;
	long downloadId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		et = (EditText) findViewById(R.id.txtMyUrl);
		final String myHTTPUrl = et.getText().toString();

		isWIFI = (CheckBox) findViewById(R.id.chkWaitForWifi);

		btnDownload = (Button) findViewById(R.id.btnDownload);

		mProgressBar = (ProgressBar) findViewById(R.id.myDownloadProgress);

		mDownloadProgressPercent = (TextView) findViewById(R.id.myDownloadProgressPercent);

		btnDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				DownloadManager.Request request = new DownloadManager.Request(
						Uri.parse(myHTTPUrl));
				request.allowScanningByMediaScanner();
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

				if (isWIFI.isEnabled()) {
					request.setDescription("File is being downloaded over Wifi");
					request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
				} else {
					request.setDescription("File is being downloaded over Mobile Network");
					request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
				}

				String nameOfFile = URLUtil.guessFileName(myHTTPUrl, null,
						MimeTypeMap.getFileExtensionFromUrl(myHTTPUrl));

				request.setDestinationInExternalPublicDir(
						Environment.DIRECTORY_DOWNLOADS, nameOfFile);

				final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

				downloadId = manager.enqueue(request);

				new Thread(new Runnable() {

					@Override
					public void run() {

						boolean downloading = true;

						while (downloading) {

							DownloadManager.Query q = new DownloadManager.Query();
							q.setFilterById(downloadId);

							Cursor cursor = manager.query(q);
							cursor.moveToFirst();

							int bytes_downloaded = cursor.getInt(cursor
									.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
							int bytes_total = cursor.getInt(cursor
									.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

							if (cursor.getInt(cursor
									.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
								downloading = false;
							}
							if (null != cursor) {
								cursor.close();
							}

							final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									mProgressBar
											.setProgress(dl_progress < 0 ? 0
													: dl_progress);
									mDownloadProgressPercent.setText(Integer
											.toString(dl_progress < 0 ? 0
													: dl_progress)
											+ "%");
								}
							});
						}
					}
				}).start();
			}

		});

		mWifiStatus = (TextView) findViewById(R.id.myWifiStatus);

		DisplayWifiState();

		registerReceiver(myWifiReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

	};

	private final BroadcastReceiver myWifiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			@SuppressWarnings("deprecation")
			NetworkInfo networkInfo = (NetworkInfo) arg1
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				DisplayWifiState();
			}
		}
	};

	private void DisplayWifiState() {

		ConnectivityManager myConnManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo myNetworkInfo = myConnManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (myNetworkInfo.isConnected()) {
			mWifiStatus.setText("Connected");
		} else {
			mWifiStatus.setText("Not Connected");
		}

	};

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myWifiReceiver);

	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
	}
}