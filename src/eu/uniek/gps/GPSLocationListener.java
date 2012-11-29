package eu.uniek.gps;

import org.osmdroid.util.GeoPoint;

import eu.uniek.osmbonuspacktest.MapActivity;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class GPSLocationListener implements LocationListener {  
	private Location currentLocation;
	private MapActivity mapActivity;

	public GPSLocationListener(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
		// TODO Auto-generated constructor stub
	}
	public void onLocationChanged(Location location) {  
		this.currentLocation = location;
        mapActivity.updatedLocation(getLocationInGeoPoint());
	}
	
	public GeoPoint getLocationInGeoPoint() {
		return new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
	}
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}
}
