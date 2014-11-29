package edu.rutgers.erosen;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CV_Benchmark extends Activity implements CvCameraViewListener2 {
	private static final String TAG = "CVBenchmark::Activity";
	private static final String DIR = "CVBenchmark";

	private static final int VIEW_MODE_RGB = 0;
	private static final int VIEW_MODE_GRAY = 1;
	private static final int VIEW_MODE_CANNY = 2;
	private static final int VIEW_MODE_SOBEL = 4;
	private static final int VIEW_MODE_OPTICAL_FLOW = 5;
	private static final int VIEW_MODE_SUBTRACTION = 6;
	private static final int VIEW_MODE_FEATURES = 7;

	private int mViewMode;
	private Mat mRgba;
	private Mat mIntermediateMat;
	private Mat mGray, mGray2;
	private Mat result;
	private Mat flow, cflow, uflow;

	private MenuItem mItemPreviewRGB;
	private MenuItem mItemPreviewGray;
	private MenuItem mItemPreviewCanny;
	private MenuItem mItemPreviewSobel;
	private MenuItem mItemPreviewOpticalFlow;
	private MenuItem mItemPreviewFeatures;
	private MenuItem mItemPreviewSubtraction;

	private TextView mTask;
	private TextView mIP;
	private TextView mFPS;

	private Button mCaptureFrame;
	private Button mDeletePhotos;

	private CameraBridgeViewBase mOpenCvCameraView;

	private ExportPhoto myPicture;

	private int port = 8080;
	private WebServer server;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");

				mOpenCvCameraView.enableView();

			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	public CV_Benchmark() {
		super();
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.camera_grid_view);

		mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_1);
		mOpenCvCameraView.setCvCameraViewListener(this);

		mFPS = (TextView) findViewById(R.id.myFPS);
		mFPS.setText("");

		mTask = (TextView) findViewById(R.id.mytask);
		mTask.setText("Default - RGBA Color");

		mIP = (TextView) findViewById(R.id.myIP);
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
		final String formatedIpAddress = String.format(Locale.US,
				"%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
				(ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
		mIP.setText(formatedIpAddress + ":" + port);

		final File file = new File(Environment.getExternalStorageDirectory(),
				DIR);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.e(TAG, "Problem creating Image folder");

			}
		}

		mCaptureFrame = (Button) findViewById(R.id.btnCaptureFrame);
		mCaptureFrame.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
						Locale.US).format(new Date());
				Mat mPhotoExport = myPicture.getPhoto();
				Imgproc.cvtColor(mPhotoExport, mPhotoExport,
						Imgproc.COLOR_RGBA2RGB);
				Highgui.imwrite(file.getPath().toString() + "/export_"
						+ timeStamp + ".png", mPhotoExport);
				Toast toast = Toast.makeText(CV_Benchmark.this, "Photo Saved",
						Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		
		mDeletePhotos = (Button) findViewById(R.id.btnDelete);
		mDeletePhotos.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				for(File files: file.listFiles()) files.delete();
				
				Toast toast = Toast.makeText(CV_Benchmark.this, "All photos have been deleted.",
						Toast.LENGTH_SHORT);
				toast.show();
			}
		});
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.i(TAG, "called onCreateOptionsMenu");
		mItemPreviewRGB = menu.add("Preview RGB");
		mItemPreviewGray = menu.add("Preview GRAY");
		mItemPreviewCanny = menu.add("Canny");
		mItemPreviewSobel = menu.add("Sobel Filter");
		mItemPreviewOpticalFlow = menu.add("Optical Flow");
		mItemPreviewSubtraction = menu.add("Differential Motion");
		mItemPreviewFeatures = menu.add("Find features");
		return true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();

		if (server != null)
			server.stop();
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

		try {
			server = new WebServer(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onDestroy() {
		super.onDestroy();
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
		cflow = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		mGray2 = new Mat(height, width, CvType.CV_8UC1);
		result = new Mat(height, width, CvType.CV_8UC1);
		uflow = new Mat(height, width, CvType.CV_32FC2);
		flow = new Mat(height, width, CvType.CV_32FC2);

		myPicture = new ExportPhoto(height, width);

	}

	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
		mGray2.release();
		result.release();
		mIntermediateMat.release();
		flow.release();
		uflow.release();
		cflow.release();

		myPicture.release();
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		final int viewMode = mViewMode;
		trackFPS myFPS = new trackFPS();

		myFPS.start();

		switch (viewMode) {
		case VIEW_MODE_GRAY:
			// input frame has gray scale format
			Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA,
					4);

			break;
		case VIEW_MODE_RGB:
			// input frame has RBGA format
			mRgba = inputFrame.rgba();
			break;

		case VIEW_MODE_CANNY:
			// input frame has gray scale format
			mRgba = inputFrame.rgba();
			Scalar green = new Scalar(0, 1, 0, 0);
			Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
			Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA,
					4);
			Core.multiply(mRgba, green, mRgba);

			break;

		case VIEW_MODE_SOBEL:
			mGray = inputFrame.gray();
			Scalar red = new Scalar(1, 0, 0, 0);
			Imgproc.Sobel(mGray, mIntermediateMat, CvType.CV_8U, 1, 1);
			Core.convertScaleAbs(mIntermediateMat, mIntermediateMat, 10, 0);
			Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA,
					4);
			Core.multiply(mRgba, red, mRgba);

			break;

		case VIEW_MODE_OPTICAL_FLOW:

			int step = 32;
			Scalar color2 = new Scalar(0, 255, 0, 255);
			mGray = inputFrame.gray();

			if (mGray2 != null) {
				Video.calcOpticalFlowFarneback(mGray, mGray2, uflow, 0.5, 3,
						15, 3, 5, 1.5, 0);
				Imgproc.cvtColor(mGray2, cflow, Imgproc.COLOR_GRAY2BGRA, 4);
				uflow.copyTo(flow);
				for (int y = 0; y < cflow.rows(); y += step) {
					for (int x = 0; x < cflow.cols(); x += step) {
						Point fxy = new Point(flow.get(x, y));
						Point point = new Point(x, y);
						Point calc = new Point(Math.round(x + fxy.x),
								Math.round((y + fxy.y)));
						Core.line(cflow, point, calc, color2);
						Core.circle(cflow, point, 2, color2, -1);
					}

				}
			}
			cflow.copyTo(mRgba);
			mGray2 = inputFrame.gray();

			break;

		case VIEW_MODE_SUBTRACTION:

			mGray = inputFrame.gray();
			Scalar blue = new Scalar(0, 0, 1, 0);

			if (mGray2 != null) {
				Core.subtract(mGray, mGray2, result);
				Imgproc.threshold(result, result, 80, 255,
						Imgproc.THRESH_BINARY);
			}

			Imgproc.cvtColor(result, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);

			Core.multiply(mRgba, blue, mRgba);

			mGray2 = inputFrame.gray();

			break;

		case VIEW_MODE_FEATURES:

			mRgba = inputFrame.rgba();
			Scalar color = new Scalar(255, 0, 0, 255);

			FeatureDetector detector = FeatureDetector
					.create(FeatureDetector.FAST);
			MatOfKeyPoint keypoints = new MatOfKeyPoint();
			detector.detect(inputFrame.gray(), keypoints);
			KeyPoint kp[] = keypoints.toArray();
			for (int i = 0; i < kp.length; i++) {

				Point point = new Point(kp[i].pt.x, kp[i].pt.y);
				Core.circle(mRgba, point, 10, color);
			}

			break;
		}

		myFPS.getFPS();
		myPicture.setPhoto(mRgba);

		return mRgba;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mItemPreviewRGB) {
			mViewMode = VIEW_MODE_RGB;
			mTask.setText("Default - RGBA Color");
		} else if (item == mItemPreviewGray) {
			mViewMode = VIEW_MODE_GRAY;
			mTask.setText("Grayscale");
		} else if (item == mItemPreviewCanny) {
			mViewMode = VIEW_MODE_CANNY;
			mTask.setText("Canny Edge Detection");
		} else if (item == mItemPreviewSobel) {
			mViewMode = VIEW_MODE_SOBEL;
			mTask.setText("Sobel Edge Filter");
		} else if (item == mItemPreviewOpticalFlow) {
			mViewMode = VIEW_MODE_OPTICAL_FLOW;
			mTask.setText("Optical Flow");
		} else if (item == mItemPreviewSubtraction) {
			mViewMode = VIEW_MODE_SUBTRACTION;
			mTask.setText("Differential Frame Analysis");
		} else if (item == mItemPreviewFeatures) {
			mViewMode = VIEW_MODE_FEATURES;
			mTask.setText("Fast Feature Extraction");
		}

		return true;
	}

	private class trackFPS {
		private long start_time;
		private DecimalFormat df = new DecimalFormat("###");

		public trackFPS() {

		}

		public void start() {
			start_time = System.nanoTime();

		}

		public void getFPS() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mFPS.setText(df.format(1000000000.0 / (System.nanoTime() - start_time)));
				}
			});

		}

	}

	private class ExportPhoto {

		private Mat exportPhoto;

		public ExportPhoto(int height, int width) {
			exportPhoto = new Mat(height, width, CvType.CV_8UC4);
		}

		public void release() {
			exportPhoto.release();
		}

		public void setPhoto(Mat mRgba) {
			exportPhoto = mRgba;
		}

		public Mat getPhoto() {
			return exportPhoto;
		}

	}

}
