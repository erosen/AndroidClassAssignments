package edu.rutgers.friendfinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends Activity {

	private TextView mAddress, mLongLat, mDistance, mAcc;
	private ListView myList;
	private LocationsDataSource datasource;
	private ImageView imgMarker;

	private LocationManager locationManager1, locationManager2;
	private LocationListener locationListener1, locationListener2;

	private Geocoder geocoder;

	private GoogleMap googleMap;

	private CameraUpdate yourLocation;

	private static double[] flatitude = { 40.517838, 40.513189, 40.495527,
			40.497127 };
	private static double[] flongitude = { -74.465297, -74.433849, -74.467142,
			-74.417056 };

	boolean zoom = true;

	private Bitmap bm[] = new Bitmap[4];
	private Marker marker;
	private View markerView;
	private String[] friends = {
			"http://www.winlab.rutgers.edu/~huiqing/mickey.png",
			"http://www.winlab.rutgers.edu/~huiqing/donald.jpg",
			"http://www.winlab.rutgers.edu/~huiqing/goofy.png",
			"http://www.winlab.rutgers.edu/~huiqing/garfield.jpg" };

	private LatLng coords;
	private LatLng coordinate;
	private double latitude;
	private double longitude;

	private String text;
	private Context con;

	private List<Locations> values;
	ArrayAdapter<Locations> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mLongLat = (TextView) findViewById(R.id.myLongLat);
		mLongLat.setTextColor(Color.BLUE);
		mLongLat.setText("");

		mAddress = (TextView) findViewById(R.id.myAddress);
		mAddress.setTextColor(Color.BLUE);
		mAddress.setText("");
		mAddress.setTextSize(6);

		mAcc = (TextView) findViewById(R.id.myAcc);
		mAcc.setTextColor(Color.BLUE);
		mAcc.setText("");
		mAcc.setTextSize(7);

		myList = (ListView) findViewById(R.id.list);

		datasource = new LocationsDataSource(this);
		datasource.open();

		values = datasource.getAllLocations();
		adapter = new ArrayAdapter<Locations>(this,
				android.R.layout.simple_list_item_1, values);

		myList.setAdapter(adapter);

		// set the map to a MapFragment
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

		}

		// Add markers for list of friends
		for (int i = 0; i < flatitude.length; i++) {
			googleMap.addMarker(new MarkerOptions().position(new LatLng(
					flatitude[i], flongitude[i])));

		}

		geocoder = new Geocoder(this, Locale.getDefault());
		locationManager1 = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager2 = locationManager1;
		geocoder = new Geocoder(this, Locale.getDefault());

		locationListener1 = new LocationListener() {
			public void onLocationChanged(Location location) {

				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				double acc = location.getAccuracy();

				mLongLat.setText(Double.toString(longitude) + " / "
						+ Double.toString(latitude));

				mAcc.setText("Accuracy: "
						+ Double.toString(Math.round(acc * 100) / 100));

				getAddress ga = new getAddress(con, longitude, latitude);
				ga.execute();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationListener2 = new LocationListener() {
			public void onLocationChanged(Location location) {
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				double acc = location.getAccuracy();

				mLongLat.setText(Double.toString(longitude) + " / "
						+ Double.toString(latitude));
				mAcc.setText("Accuracy: "
						+ Double.toString(Math.round(acc * 100) / 100));

				getAddress ga = new getAddress(con, longitude, latitude);
				ga.execute();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager1.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				60000, 0, locationListener1);

		locationManager2.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 60000, 0, locationListener2);

		googleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

			@SuppressLint("InflateParams")
			@Override
			public View getInfoContents(Marker m) {

				markerView = getLayoutInflater().inflate(R.layout.markerview,
						null);
				imgMarker = (ImageView) markerView.findViewById(R.id.imgMarker);
				mDistance = (TextView) markerView.findViewById(R.id.mDistance);
				mDistance.setTextSize(6);

				coords = m.getPosition();

				for (int i = 0; i < flatitude.length; i++) {

					if (Math.abs(coords.latitude - flatitude[i]) < 0.001) {
						imgMarker.setImageBitmap(bm[i]);
					}
				}

				float[] dist = new float[1];

				if (longitude == 0)
					mDistance.setText("No Location");
				else {
					Location.distanceBetween(latitude, longitude,
							coords.latitude, coords.longitude, dist);
					mDistance.setText("" + (float) Math.round(dist[0] * 100)
							/ 100 + " meters");
				}

				marker = m;
				return markerView;

			}

			@Override
			public View getInfoWindow(Marker m) {

				return null;
			}
		});

		googleMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng location) {

				MarkerOptions markerOptions = new MarkerOptions();
				// Set marker at location
				markerOptions.position(location);

				// center the map to current location
				googleMap.animateCamera(CameraUpdateFactory.newLatLng(location));

				// if the marker has something there, show the friend
				// information
				if (marker != null)
					marker.showInfoWindow();

			}
		});

		for (int i = 0; i < 4; i++) {
			downloadImage(imgMarker, i);
		}
	}

	public void onClick(View view) {
		@SuppressWarnings("unchecked")
		ArrayAdapter<Locations> adapter = (ArrayAdapter<Locations>) myList
				.getAdapter();
		Locations location = null;

		switch (view.getId()) {
		case R.id.btnCheckIn:

			location = datasource.createLocation(String.valueOf(longitude),
					String.valueOf(latitude));
			adapter.add(location);

			break;
		case R.id.btnDelete:

			if (adapter.getCount() > 0) {
				location = adapter.getItem(0);
				datasource.deleteLocation(location);
				adapter.remove(location);
			}

			break;

		case R.id.btnUpdateLocation:

			// Update immediately
			locationManager1.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener1);
			locationManager2.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, locationListener2);

			// delay some time
			for (int i = 0; i < 100000; i++) {

			}
			// set back to every minute
			locationManager1.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 60000, 0, locationListener1);
			locationManager2.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 60000, 0,
					locationListener2);

			break;
		}
		adapter.notifyDataSetChanged();
	}

	public void downloadImage(View iv, int index) {
		// iv.setImageBitmap(null);

		new DownloadImageTask(index).execute(friends[index]);

	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

		private int index;

		public DownloadImageTask(int i) {
			index = i;
		}

		@Override
		protected Bitmap doInBackground(String... url) {

			return loadImageFromNetwork(url[0]);
		}

		@Override
		protected void onPostExecute(Bitmap result) {

			bm[index] = result;

			// iv.setImageBitmap(result);
		}
	}

	private Bitmap loadImageFromNetwork(String url) {
		Bitmap bitmap = null;

		try {
			bitmap = BitmapFactory.decodeStream((InputStream) new URL(url)
					.getContent());

		} catch (Exception e) {

			e.printStackTrace();
		}

		return bitmap;
	}

	private class getAddress extends AsyncTask<Location, Void, Void> {

		public getAddress(Context context, double lon, double lat) {
			super();
			latitude = lat;
			longitude = lon;
		}

		@Override
		protected Void doInBackground(Location... params) {
			try {

				List<Address> addresses = geocoder.getFromLocation(latitude,
						longitude, 1);

				if (addresses != null) {
					Address address = addresses.get(0);
					StringBuilder buildMeUp = new StringBuilder("");
					for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
						buildMeUp.append(address.getAddressLine(i)).append(",");
					}
					text = buildMeUp.toString();
				} else {
					text = "No Address" + "\n";
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				text = "Null Address" + "\n";
			}

			runOnUiThread(new Runnable() {
				public void run() {
					coordinate = new LatLng(latitude, longitude);
					yourLocation = CameraUpdateFactory.newLatLngZoom(
							coordinate, 10);
					// if(zoom == true)
					googleMap.animateCamera(yourLocation);
					mAddress.setText(text);
				}
			});
			zoom = false;
			return null;
		}
	}

	@Override
	protected void onResume() {
		datasource.open();
		super.onResume();
	}

	@Override
	protected void onPause() {
		datasource.close();
		super.onPause();
	}

}
