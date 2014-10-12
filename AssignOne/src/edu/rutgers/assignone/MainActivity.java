package edu.rutgers.assignone;

import android.support.v7.app.ActionBarActivity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
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

	TextView mWifiStatus, mDownloadProgressPercent, mStatus;
	EditText et;
	String myHTTPUrl;
	CheckBox isWifi;
	Button btnDownload;
	ProgressBar mProgressBar;
	long downloadId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		et = (EditText) findViewById(R.id.txtMyUrl);

		mStatus = (TextView) findViewById(R.id.myStatus);
		mStatus.setTextColor(Color.BLUE);
		mStatus.setText("Press the button to download");

		isWifi = (CheckBox) findViewById(R.id.chkWaitForWifi);

		btnDownload = (Button) findViewById(R.id.btnDownload);

		mProgressBar = (ProgressBar) findViewById(R.id.myDownloadProgress);

		mDownloadProgressPercent = (TextView) findViewById(R.id.myDownloadProgressPercent);

		btnDownload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				final String myHTTPUrl = et.getText().toString();

				DownloadManager.Request request = new DownloadManager.Request(
						Uri.parse(myHTTPUrl));
				request.allowScanningByMediaScanner();
				request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

				mStatus.setText("Beginning Download");

				if (isWifi.isChecked()) {
					request.setDescription("File is being downloaded over Wifi");
					request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
				} else {
					request.setDescription("File is being downloaded over any available network");
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
									
									if (mProgressBar.getProgress() == 100) {

										mStatus.setText("Download Completed");
									}
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
			mWifiStatus.setTextColor(Color.GREEN);
		} else {
			mWifiStatus.setText("Waiting for Wifi...");
			mWifiStatus.setTextColor(Color.RED);
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(myWifiReceiver);

		System.exit(0);
	}

}