package net.app.igusaya.maptest2;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import net.app.igusaya.maptest2.model.LocationPointBeans;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener, GoogleMap.OnMapClickListener{

	private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private LocationManager mLocationManager;
	private LatLng mStartLatlng, mLatlng;
	private MarkerOptions mMarkerOptions = new MarkerOptions();
	private List<LocationPointBeans> mLocationPointList = new ArrayList<LocationPointBeans>();
	private PolylineOptions mPolylineOptions;
	private Button mButton;
	private UiSettings mUiSettings;

	private long mStartTime, mPassingTime;
	private boolean mIsSetStartPoint = false;
	private int distance;
	private double mLatitude, mLongitude, mStartLatitude, mStartLongitude;

	int i = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);
		setUpMapIfNeeded();

		mUiSettings = mMap.getUiSettings();
		mUiSettings.setZoomControlsEnabled(true);

		mButton = (Button)findViewById(R.id.button);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getBaseContext(),
						"位置：" + mLatitude + "    " + mLongitude + "　呼び出し回数：" + i + "　移動距離：" + distance + "M",
						Toast.LENGTH_LONG).show();
			}
		});
		if(!mIsSetStartPoint)
			mMap.setOnMapClickListener(this);
	}

	/**
	 * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
	 * installed) and the map has not already been instantiated.. This will ensure that we only ever
	 * call {@link #setUpMap()} once when {@link #mMap} is not null.
	 * <p/>
	 * If it isn't installed {@link SupportMapFragment} (and
	 * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
	 * install/update the Google Play services APK on their device.
	 * <p/>
	 * A user can return to this FragmentActivity after following the prompt and correctly
	 * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
	 * have been completely destroyed during this process (it is likely that it would only be
	 * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
	 * method in {@link #onResume()} to guarantee that it will be called.
	 */
	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
					.getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				setUpMap();
			}
		}
	}

	/**
	 * This is where we can add markers or lines, add listeners or move the camera. In this case, we
	 * just add a marker near Africa.
	 * <p/>
	 * This should only be called once and when we are sure that {@link #mMap} is not null.
	 */
	private void setUpMap() {
		MapsInitializer.initialize(this);   //初期化(作法)

		//現在位置の取得
		mLocationManager = (LocationManager)getSystemService(this.LOCATION_SERVICE);
		boolean isGpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//		boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if(isGpsEnabled){
			Toast.makeText(this, "GPS有効", Toast.LENGTH_SHORT).show();
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300, 1, this);
		}
//		if(isNetworkEnabled){
//			Toast.makeText(this, "Wifi有効", Toast.LENGTH_SHORT).show();
//			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,10,this);
//		}
	}

	@Override
	public void onLocationChanged(Location location) {
		i++;

		//現在の情報を取得
		mLatitude = location.getLatitude();
		mLongitude = location.getLongitude();
		mLatlng = new LatLng(mLatitude, mLongitude);
		mPassingTime = System.currentTimeMillis();

		//スタート位置情報の取得
		if(mStartLatitude == 0){
			mStartLatitude = mLatitude;
			mStartLongitude = mLongitude;
			mStartLatlng = mLatlng;
			mStartTime = mPassingTime;
		}

		//経過位置情報をBeansへ格納し、Listへ追加
		LocationPointBeans locationPointBeans = new LocationPointBeans(mLatitude, mLongitude, mPassingTime, mLatlng);
		mLocationPointList.add(locationPointBeans);

		//現在位置にフォーカス
		mMap.clear();
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mLatlng, 19);
		mMap.moveCamera(cameraUpdate);

		//スタート位置と現在位置にマーカーセット
		mMap.addMarker(mMarkerOptions
				.position(mStartLatlng)
				.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
				.title("開始地点"));
		mMap.addMarker(new MarkerOptions()
				.position(mLatlng)
				.title("現在地"));

		//経過ルートを表示
		mPolylineOptions = new PolylineOptions();
		double oldLatitude = 0;
		double oldLongitude = 0;
		float[] courseResults = new float[1];
		distance = 0;
		for(LocationPointBeans lpb : mLocationPointList){
			//前周値と同値か確認
			if(lpb.getLatitude() != oldLatitude && lpb.getLongitude() != oldLongitude) {
				mPolylineOptions.add(lpb.getLatLng());        //経過ポイントを取得
				if (oldLatitude != 0) {
					Location.distanceBetween(oldLatitude, oldLongitude, lpb.getLatitude(), lpb.getLongitude(), courseResults);
					distance += courseResults[0];
				}
			}
			//次周判別用値を取得
			oldLatitude = lpb.getLatitude();
			oldLongitude = lpb.getLongitude();
		}
		mPolylineOptions.color(0x330000ff);
		mPolylineOptions.width(5);
		mPolylineOptions.geodesic(true); // 測地線で表示
		mMap.addPolyline(mPolylineOptions);

		// 距離計算
//		Location.distanceBetween(mLatitude, mLongitude, 35.689488, 139.691706, mResults);
//		Toast.makeText(this, "距離：" + mResults[0]/1000 + "Km", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onMapClick(LatLng latLng) {
		mIsSetStartPoint = true;
		mStartLatitude = latLng.latitude;
		mStartLongitude = latLng.longitude;

		//スタート地点登録
		if(mLocationPointList != null){
			mLocationPointList = new ArrayList<LocationPointBeans>();
			mStartLatlng = latLng;
			mStartTime = System.currentTimeMillis();
		}
		mLocationPointList = new ArrayList<LocationPointBeans>();
		LocationPointBeans locationPointBeans = new LocationPointBeans(mStartLatitude, mStartLongitude, mStartTime, mStartLatlng);
		mLocationPointList.add(locationPointBeans);

		Toast.makeText(getBaseContext(), "スタート位置調整", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	@Override
	public void onProviderEnabled(String provider) {}
	@Override
	public void onProviderDisabled(String provider) {}
	@Override
	protected void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(this);
	}
}