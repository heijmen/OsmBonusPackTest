package eu.uniek.osmbonuspacktest;

import org.osmdroid.util.GeoPoint;

import com.wwy.gyroguide.database.DatabaseHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import eu.uniek.compas.Kompas;
import eu.uniek.compas.KompasListener;
import eu.uniek.gps.GPSLocationListener;
import eu.uniek.route.RouteHandler;
import eu.uniek.route.RouteListener;

public class MapActivity extends Activity  {

	private TextView textView;
	private TextView textView1;
	private GeoPoint destination = new GeoPoint(52.093367,5.116061);
	private Context context = this;
	private Kompas kompas;
	
	private GPSLocationListener mLocationListener = new GPSLocationListener(this);
	//private RouteHandler mRouteHandler;
	private RoadMaker mRoadMaker;
	
	private LocationManager mLocationManager;
	
	private Vibrator mVibratorService;
	private DatabaseHandler mDatabaseHandler = new DatabaseHandler(this, "osmbonuspackdatabase", 1);

	
	@Override
	protected void onResume() {
		super.onResume();
		kompas.startListening();
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = mLocationManager.getBestProvider(c, true);
		mLocationManager.requestLocationUpdates(provider, 500, 1, mLocationListener);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
//		Intent i = new Intent(this, MapTestActivity.class);
//		startActivity(i);
		
		kompas = new Kompas(this, new KompasListener() {
			public void onSensorChanged(float azimuth) {
				if(mRoadMaker != null && mRoadMaker.getCurrentDestination() != null) {
					drawImage(new GeoPoint(mLocationListener.getCurrentLocation().getLatitude(), mLocationListener.getCurrentLocation().getLongitude()), mRoadMaker.getCurrentDestination(), azimuth);
				}
			}
		});
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		textView = (TextView) findViewById(R.id.LOL); 
		textView1 = (TextView) findViewById(R.id.textView1);
		
		mVibratorService = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		
		
		Button button = (Button) findViewById(R.id.startRouteButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(mLocationListener.getCurrentLocation() != null) {
					mRoadMaker = new RoadMaker(mLocationListener, destination, new RoadMakerListener() {
						
						public void onDestinationReached() {
							mVibratorService.vibrate(10000);
						}
						
						public void onDestinationChanged() {
							mVibratorService.vibrate(1000);
						}
						
					}, mDatabaseHandler); 
					mRoadMaker.startRoute();
				} else {
					Toast.makeText(context, "No currentLocation", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}
	public void drawImage(GeoPoint startPoint, GeoPoint endPoint, float azimuth) {
		DrawView drawView = (DrawView) findViewById(R.id.drawView1);
		drawView.setBackgroundColor(Color.WHITE);
		int heijmething = (int) getAngleBetweenGeoPoints(startPoint, endPoint, azimuth);
		drawView.drawTheThing(heijmething);
		textView.setText(kompas.getAzimuth() +" Heijmenthing " + heijmething + " " + mLocationListener.getCurrentLocation());
	}

	public double getAngleBetweenGeoPoints(GeoPoint currentLocation, GeoPoint destination, float azimuth) {
		double angle = currentLocation.bearingTo(destination) - azimuth;
		if (angle < 0) {
			angle += 360; 
		}
		return angle;
	}

	public void updatedLocation(GeoPoint currentLocation) {
		textView.setText("CurrentLocation lat/long" + currentLocation.getLatitudeE6() + " " + currentLocation.getLongitudeE6());
	}
	public TextView getTextView() {
		return textView;
	}


}
