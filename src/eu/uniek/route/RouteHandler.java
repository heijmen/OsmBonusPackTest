package eu.uniek.route;

import java.util.ArrayList;

import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import android.content.Context;
import android.os.Handler;
import android.os.Vibrator;
import eu.uniek.gps.GPSHandler;
import eu.uniek.gps.GPSLocationListener;

public class RouteHandler {
	
	private Road mRoad;
	private int mCurrentDestinationIndex;
	private GPSLocationListener mLocationListener;
	private Context mContext;
	private Handler mUpdateRoadHandler;
	private RouteListener mRouteListener;
	
	public GeoPoint getCurrentDestination() {
		if(mCurrentDestinationIndex < mRoad.mNodes.size()) {
			return mRoad.mNodes.get(mCurrentDestinationIndex).mLocation;
		}
		return null;
	}
	
	private Runnable mUpdateRunnable = new Runnable() {
		public void run() {
			updateCurrentRoute();
			mUpdateRoadHandler.postDelayed(mUpdateRunnable, 1000);
		}
	};
	private GeoPoint destination;
	
	public RouteHandler(GPSLocationListener locationListener, Context context, RouteListener routeListener, GeoPoint destination) {
		this.mLocationListener = locationListener;
		this.mContext = context;
		this.destination = destination;
		this.mUpdateRoadHandler = new Handler();
		this.mRouteListener = routeListener;
		
		mRoad = getRoute();
		mUpdateRunnable.run();
	}
	
	public Road getRoute() {
		RoadManager roadManager = new GoogleRoadManager();
		roadManager.addRequestOption("mode=walking");
		ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
		GeoPoint startPoint = new GeoPoint(mLocationListener.getCurrentLocation().getLatitude(), 
				mLocationListener.getCurrentLocation().getLongitude());
		GeoPoint endPoint = new GeoPoint(52.09341,5.116033);
		waypoints.add(startPoint); 
		waypoints.add(endPoint);
		Road road = roadManager.getRoad(waypoints);
		mCurrentDestinationIndex = 0;
		return road;
	}
	
	public void updateCurrentRoute() {
		if(mRoad != null && mLocationListener.getCurrentLocation() != null) {
			Vibrator v = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
			if(!(mCurrentDestinationIndex < mRoad.mNodes.size())) {
				v.vibrate(10000);
				return;
			}
			if(GPSHandler.distanceBetween(mLocationListener.getLocationInGeoPoint(), mRoad.mNodes.get(mCurrentDestinationIndex).mLocation) < 20) {
				v.vibrate(1000);
				mCurrentDestinationIndex++;
				if(!(mCurrentDestinationIndex < mRoad.mNodes.size())) {
					mRouteListener.onDestinationArrived();
				}

			}
		}
	}

}
