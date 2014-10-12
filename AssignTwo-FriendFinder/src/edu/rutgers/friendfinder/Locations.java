package edu.rutgers.friendfinder;

public class Locations {
	private long id;
	private String longitude;
	private String latitude;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLatitude() {
		return latitude;
	}
	
	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	// Will be used by the ArrayAdapter in the ListView
	@Override
	public String toString() {
		return longitude + " / " + latitude ;
	}
}
