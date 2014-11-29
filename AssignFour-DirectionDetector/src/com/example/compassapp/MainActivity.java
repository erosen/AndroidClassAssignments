package com.example.compassapp;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	// define the display assembly compass picture
	private ImageView image;

	// record the compass picture angle turned
	private float currentDegree = 0f;

	// device sensor manager
	private SensorManager mSensorManagerOrientation;
	private SensorManager mSensorManagerAccel;

	TextView tvHeading;
	TextView tvX;
	TextView tvY;
	TextView tvZ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		//
		image = (ImageView) findViewById(R.id.imageViewCompass);

		// TextView that will tell the user what degree is he heading
		tvHeading = (TextView) findViewById(R.id.tvHeading);
		tvX = (TextView) findViewById(R.id.myX);
		tvY = (TextView) findViewById(R.id.myY);
		tvZ = (TextView) findViewById(R.id.myZ);

		// initialize your android device sensor capabilities
		mSensorManagerOrientation = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensorManagerAccel = (SensorManager) getSystemService(SENSOR_SERVICE);
		
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();

		// for the system's orientation sensor registered listeners
		mSensorManagerOrientation.registerListener(this,
				mSensorManagerOrientation.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);
		
		mSensorManagerAccel.registerListener(this,
				mSensorManagerAccel.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// to stop the listener and save battery
		mSensorManagerOrientation.unregisterListener(this);
		mSensorManagerAccel.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {

		Sensor mySensor = event.sensor;

		// get the angle around the z-axis rotated
		float degree;
		float x, y, z;
		
		if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			x = Math.round(event.values[0]);
			y = Math.round(event.values[1]);
			z = Math.round(event.values[2]);

			tvX.setText(Float.toString(x));
			tvY.setText(Float.toString(y));
			tvZ.setText(Float.toString(z));

		}

		if (mySensor.getType() == Sensor.TYPE_ORIENTATION) {

			degree = Math.round(event.values[0]);

			tvHeading
					.setText("Heading: " + Float.toString(degree) + " degrees");

			// create a rotation animation (reverse turn degree degrees)
			RotateAnimation ra = new RotateAnimation(currentDegree, -degree,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);

			// how long the animation will take place
			ra.setDuration(210);

			// set the animation after the end of the reservation status
			ra.setFillAfter(true);

			// Start the animation
			image.startAnimation(ra);
			currentDegree = -degree;
		}

	}
}
