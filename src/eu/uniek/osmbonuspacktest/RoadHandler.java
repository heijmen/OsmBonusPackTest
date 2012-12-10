package eu.uniek.osmbonuspacktest;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import android.os.Handler;

import com.wwy.gyroguide.database.DatabaseHandler;

import eu.uniek.gps.GPSHandler;
import eu.uniek.gps.GPSLocationListener;

public class RoadHandler {

	protected Road mRoad;
	private int mCurrentDestinationIndex;
	private GPSLocationListener mLocationListener;
	private Handler wayPointHandler = new Handler();
	private static final int ALLOWED_DISTANCE = 20;
	private RoadHandlerListener roadHandlerListener;

	private Runnable updateWayPointRunnable = new Runnable() {
		public void run() {
			checkNearWayPoint();
			wayPointHandler.postDelayed(updateWayPointRunnable, 3000);
		}
	};

	public RoadHandler(RoadHandlerListener roadHandlerListener) {
		this.roadHandlerListener = roadHandlerListener;

	}

	public void startRoute(GeoPoint currentLocation, GeoPoint destination, GPSLocationListener locationListener) {
		this.mRoad = getRoute(currentLocation, destination);
		this.mLocationListener = locationListener;
		updateWayPointRunnable.run();
	}


	private Road getRoute(GeoPoint currentLocation, GeoPoint destination) {
		RoadManager roadManager = new GoogleRoadManager();
		roadManager.addRequestOption("mode=walking");
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();

		waypoints.add(currentLocation); 
		waypoints.add(destination);

		Road road = roadManager.getRoad(waypoints);
		mCurrentDestinationIndex = 0;
		return road;
	}

	private void checkNearWayPoint() {
		if(getCurrentDestination() == null) {
			roadHandlerListener.onDestinationReached();
			wayPointHandler.removeCallbacks(updateWayPointRunnable);
			return;
		}

		if(GPSHandler.distanceBetween(mLocationListener.getLocationInGeoPoint(), getCurrentDestination()) < ALLOWED_DISTANCE) {
			mCurrentDestinationIndex++;
		}
	}
	public GeoPoint getCurrentDestination() {
		if(mCurrentDestinationIndex < mRoad.mNodes.size()) {
			return mRoad.mNodes.get(mCurrentDestinationIndex).mLocation;
		} else {
			return null;
		}
	}
	public void stopRouting() {
		wayPointHandler.removeCallbacks(updateWayPointRunnable);
	}

}

class RoadMaker implements RoadHandlerListener {
	private List<GeoPoint> mBreadcrumbs;
	private List<GeoPoint> mHerkenningspunten;
	private GeoPoint destination;
	private GeoPoint currentDestination;
	private DatabaseHandler mDatabaseHandler;
	private GPSLocationListener mLocationListener;
	private Handler roadUpdateHandler = new Handler();
	private RoadHandler mCurrentRoadHandler;

	private boolean breadcrumbOrHerkenningspuntFound = false;
	private RoadMakerListener mRoadMakerListener;

	private static final int BREADCRUMBDISTANCE = 20;
	private static final int HERKENNINGSPUNTDISTANCE = 50;
	private static final int WAYPOINTDISTANCE = 20;

	private Runnable updateRoadRunnable = new Runnable() {
		public void run() {
			checkNearBreadCrumbOrHerkenningsPunt();
			roadUpdateHandler.postDelayed(updateRoadRunnable, 5000);
		}
	};

	public RoadMaker(GPSLocationListener locationListener, 
			GeoPoint destination, 
			RoadMakerListener roadMakerListener, DatabaseHandler databaseHandler) {
		this.mLocationListener = locationListener;
		this.destination = destination;
		this.mRoadMakerListener = roadMakerListener;
		this.mDatabaseHandler = databaseHandler;
		while(true) {
			if(locationListener.getLocationInGeoPoint() != null) {
				setHerkenningsPuntenAndBreadcrumbs(locationListener.getLocationInGeoPoint(), destination);
				break;
			}
		}

	}
	/**
	 * Call this method to start the route.
	 * Only call once!!!
	 */
	public void startRoute() {
		createNewRoadHandler(mLocationListener.getLocationInGeoPoint(), destination);
		updateRoadRunnable.run();
	}

	private void createNewRoadHandler(GeoPoint currentLocation, GeoPoint destination) {
		RoadHandler roadHandler = new RoadHandler(this);
		roadHandler.startRoute(currentLocation, destination, mLocationListener);
		this.mCurrentRoadHandler = roadHandler;
	}

	private void checkNearBreadCrumbOrHerkenningsPunt() {
		GeoPoint temp_loc = null;
		for(GeoPoint herkenningspunt : mHerkenningspunten) {
			if(GPSHandler.distanceBetween(mLocationListener.getLocationInGeoPoint(), herkenningspunt) < HERKENNINGSPUNTDISTANCE) {
				temp_loc = herkenningspunt;
				break;
			}
		}
		if(temp_loc != null) {
			mHerkenningspunten.remove(temp_loc);
		} else {
			for(GeoPoint breadcrumb : mBreadcrumbs) {
				if(GPSHandler.distanceBetween(mLocationListener.getLocationInGeoPoint(), breadcrumb) < BREADCRUMBDISTANCE) {
					temp_loc = breadcrumb;
					break;
				}
			}
			if(temp_loc != null && !breadcrumbOrHerkenningspuntFound) {
				mBreadcrumbs.remove(temp_loc);
			}
		}

		if(temp_loc != null) {
			newRoute(temp_loc, destination);
		}
	}

	private void newRoute(GeoPoint futureCurrentLocation, GeoPoint destination) {
		RoadHandler roadHandler = new RoadHandler(this);
		roadHandler.startRoute(futureCurrentLocation, destination, mLocationListener);
		for(RoadNode waypoint : roadHandler.mRoad.mNodes) {
			for(RoadNode otherwaypoint : mCurrentRoadHandler.mRoad.mNodes) {
				if(GPSHandler.distanceBetween(waypoint.mLocation, otherwaypoint.mLocation) < WAYPOINTDISTANCE) {
					return;
				}
			}
		}
		mCurrentRoadHandler.stopRouting();
		mCurrentRoadHandler = roadHandler;
		mCurrentRoadHandler.startRoute(mLocationListener.getLocationInGeoPoint(), destination, mLocationListener);
		breadcrumbOrHerkenningspuntFound = true;

	}

	private void setHerkenningsPuntenAndBreadcrumbs(GeoPoint startingPoint, GeoPoint destination) {
		int averagelat = (startingPoint.getLatitudeE6() + destination.getLatitudeE6()) / 2;
		int avaragelon = (startingPoint.getLongitudeE6() + destination.getLongitudeE6()) / 2; 

		GeoPoint middle = new GeoPoint(averagelat ,avaragelon);

		float radius = GPSHandler.distanceBetween(startingPoint, destination) / 2 + 20;

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

	public void onWaypointReached(GeoPoint newDestination) {
		currentDestination = newDestination;
	}

	public GeoPoint getCurrentDestination() {
		if(mCurrentRoadHandler != null && mCurrentRoadHandler.getCurrentDestination() != null)
			return mCurrentRoadHandler.getCurrentDestination();
		else 
			return null;
	}
	public void onDestinationReached() {
		if(breadcrumbOrHerkenningspuntFound) {
			breadcrumbOrHerkenningspuntFound = false;
			mCurrentRoadHandler.stopRouting();
			mCurrentRoadHandler = new RoadHandler(this);
			mCurrentRoadHandler.startRoute(mLocationListener.getLocationInGeoPoint(), destination, mLocationListener);
		}

		mRoadMakerListener.onDestinationReached();
	}
}

interface RoadHandlerListener {

	public void onWaypointReached(GeoPoint newDestination);
	public void onDestinationReached();

}

interface RoadMakerListener {
	public void onDestinationReached();
	public void onDestinationChanged();
}
