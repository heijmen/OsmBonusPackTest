package eu.uniek.osmbonuspacktest;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import android.os.Handler;

import com.wwy.gyroguide.database.DatabaseHandler;

import eu.uniek.gps.GPSHandler;
import eu.uniek.gps.GPSLocationListener;



public class AnotherRoadHandler {
	private static final float ALLOWED_DISTANCE = 50;
	private GPSLocationListener mLocationListener;
	private GeoPoint mDestination;
	private DatabaseHandler mDatabaseHandler;
	private Handler mUpdateRoadHandler = new Handler();
	private Road mRoad;
	private int mCurrentDestinationIndex = 0;
	private List<GeoPoint> mBreadcrumbs;
	private List<GeoPoint> mHerkenningspunten;
	private AnotherRoadHanderListener mAnotherRoadHanderListener;
	
	private Runnable mUpdateRoadRunnable = new Runnable() {
		public void run() {
			checkNearWayPoint();
			mUpdateRoadHandler.postDelayed(mUpdateRoadRunnable, 10000);
		}
	};
	
	public AnotherRoadHandler(GPSLocationListener locationListener, GeoPoint destination, DatabaseHandler databaseHandler, AnotherRoadHanderListener anotherRoadHanderListener) {
		mLocationListener = locationListener;
		mDestination = destination;
		mDatabaseHandler = databaseHandler;
		this.mAnotherRoadHanderListener = anotherRoadHanderListener;
		setHerkenningsPuntenAndBreadcrumbs();
		createRoute();
	}
	
	private void checkNearWayPoint() {
		if(getCurrentDestination() == null) {
			mAnotherRoadHanderListener.onDestinationReached();
			mUpdateRoadHandler.removeCallbacks(mUpdateRoadRunnable);
			return;
		}

		if(GPSHandler.distanceBetween(mLocationListener.getLocationInGeoPoint(), getCurrentDestination()) < ALLOWED_DISTANCE) {
			mAnotherRoadHanderListener.onWayPointReached(getCurrentDestination());
			mCurrentDestinationIndex++;
		}
	}
	
	private void createRoute() {
		RoadManager roadManager = new GoogleRoadManager();
		roadManager.addRequestOption("mode=walking");
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

		waypoints.add(mLocationListener.getLocationInGeoPoint()); 
		waypoints.add(mDestination);
		
		if(!mBreadcrumbs.isEmpty() || !mHerkenningspunten.isEmpty()) {
			int waypointCount = 1;
			String waypointsRequestOption = "";
			waypointsRequestOption += "waypoints=optimize:true";
			
			for(GeoPoint herkenningspunt : mHerkenningspunten) {
				if(waypointCount >= 8) {
					break;
				}
				waypointsRequestOption += "|";
				waypointsRequestOption += "" + herkenningspunt.getLatitudeE6() / 1e6 + "," + herkenningspunt.getLongitudeE6() / 1e6;
				waypointCount++;
			}
			waypointsRequestOption = URLEncoder.encode(waypointsRequestOption);
			roadManager.addRequestOption(waypointsRequestOption);
		}

		mRoad = roadManager.getRoad(waypoints);
		mCurrentDestinationIndex = 0;
	}

	public void startRoute() {
		mUpdateRoadRunnable.run();
	}
	
	public void stopRoute() {
		mUpdateRoadHandler.removeCallbacks(mUpdateRoadRunnable);
	}
	
	private void setHerkenningsPuntenAndBreadcrumbs() {
		int averagelat = (mLocationListener.getLocationInGeoPoint().getLatitudeE6() + mDestination.getLatitudeE6()) / 2;
		int avaragelon = (mLocationListener.getLocationInGeoPoint().getLongitudeE6() +  mDestination.getLongitudeE6()) / 2; 

		GeoPoint middle = new GeoPoint(averagelat, avaragelon);

		float radius = GPSHandler.distanceBetween(mLocationListener.getLocationInGeoPoint(),  mDestination) / 2 + 20;

		this.mBreadcrumbs = getBreadcrumbsInRange(middle, radius);
		this.mHerkenningspunten = getHerkenninspuntenInRange(middle, radius);
	}

	private List<GeoPoint> getBreadcrumbsInRange(GeoPoint startingPoint, float radius) {
		List<GeoPoint> breadcrumbs = new ArrayList<GeoPoint>();
		for(int breadcrumbsIndex = 1; breadcrumbsIndex < mDatabaseHandler.getGeoPointRowCount(); breadcrumbsIndex++) {
			GeoPoint breadcrumb = mDatabaseHandler.getGeoPoint(breadcrumbsIndex);
			if(GPSHandler.distanceBetween(startingPoint, breadcrumb) < radius) {
				breadcrumbs.add(breadcrumb);
			}
		}
		return breadcrumbs;
	}

	private List<GeoPoint> getHerkenninspuntenInRange(GeoPoint startingPoint, float radius) {
		List<GeoPoint> herkenningspunten = new ArrayList<GeoPoint>();
		for(GeoPoint herkenningspunt : mDatabaseHandler.getHerkenningPunten()) {
			if(GPSHandler.distanceBetween(startingPoint, herkenningspunt) < radius) {
				herkenningspunten.add(herkenningspunt);
			}
		}
		return herkenningspunten;
	}
	public GeoPoint getCurrentDestination() {
		if(mCurrentDestinationIndex < mRoad.mNodes.size()) {
			return mRoad.mNodes.get(mCurrentDestinationIndex).mLocation;
		} else {
			return null;
		}
	}
	
	interface AnotherRoadHanderListener {
		public void onWayPointReached(GeoPoint newWayPoint);
		public void onDestinationReached();
	}
}
