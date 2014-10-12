package edu.rutgers.friendfinder;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LocationsDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_LONGITUDE, 
			MySQLiteHelper.COLUMN_LATITUDE   };

	public LocationsDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public Locations createLocation(String longitude, String latitude) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_LONGITUDE, longitude);
		values.put(MySQLiteHelper.COLUMN_LATITUDE, latitude);
		
		long insertId = database.insert(MySQLiteHelper.TABLE_LOCATIONS, null,
				values);
		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS,
				allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
				null, null, null);
		cursor.moveToFirst();
		Locations newLocation = cursorToLocation(cursor);
		cursor.close();
		return newLocation;
	}

	public void deleteLocation(Locations location) {
		long id = location.getId();
		System.out.println("Location deleted with id: " + id);
		database.delete(MySQLiteHelper.TABLE_LOCATIONS, MySQLiteHelper.COLUMN_ID
				+ " = " + id, null);
	}

	public List<Locations> getAllLocations() {
		List<Locations> locations = new ArrayList<Locations>();

		Cursor cursor = database.query(MySQLiteHelper.TABLE_LOCATIONS,
				allColumns, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			Locations location = cursorToLocation(cursor);
			locations.add(location);
			cursor.moveToNext();
		}
		// make sure to close the cursor
		cursor.close();
		return locations;
	}

	private Locations cursorToLocation(Cursor cursor) {
		Locations location = new Locations();
		location.setId(cursor.getLong(0));
		location.setLongitude(cursor.getString(1));
		location.setLatitude(cursor.getString(2));
		return location;
	}

}
