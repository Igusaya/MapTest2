package net.app.igusaya.maptest2.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by mgt on 2015/04/06.
 * 観測地点情報のbeans
 */
public class LocationPointBeans {

	private double latitude;
	private double longitude;
	private long time;
	private LatLng latLng;

	public LocationPointBeans(double latitude, double longitude, long time, LatLng latLng) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.time = time;
		this.setLatLng(latLng);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public LatLng getLatLng() {
		return latLng;
	}

	public void setLatLng(LatLng latLng) {
		this.latLng = latLng;
	}
}
