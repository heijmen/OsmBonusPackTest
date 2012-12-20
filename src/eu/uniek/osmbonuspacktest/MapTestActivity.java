package eu.uniek.osmbonuspacktest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class MapTestActivity extends Activity {

    private MapView map;
    
    private GeoPoint currentLocation = new GeoPoint(52.102156,5.107102);
    private GeoPoint destination = new GeoPoint(52.096806,5.115002);
    
    private GeoPoint herkenningsPoint1 = new GeoPoint(52.099335,5.112759);
    private GeoPoint herkenningsPoint2 = new GeoPoint(52.101431,5.110454);
    private GeoPoint herkenningsPoint3 = new GeoPoint(52.098261,5.108544);

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_test);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true); 
        MapController mapController = map.getController();
        mapController.setCenter(currentLocation);
        mapController.setZoom(9);
        
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(currentLocation);
        waypoints.add(destination);
        ExtendedOverlayItem item = new ExtendedOverlayItem("", "", herkenningsPoint1 , map.getContext());
        List<ExtendedOverlayItem> items = new ArrayList<ExtendedOverlayItem>();
        items.add(new ExtendedOverlayItem("", "", herkenningsPoint2, map.getContext()));
        items.add(item);
        items.add(new ExtendedOverlayItem("", "", herkenningsPoint3, map.getContext()));
        items.add(item);
       ItemizedOverlay<ExtendedOverlayItem> itemoverlay = new ItemizedIconOverlay<ExtendedOverlayItem>(map.getContext(), items, null);
      map.getOverlays().add(itemoverlay);
        RoadManager roadManager = new GoogleRoadManager();
        String waypointRequestOption = "";
        waypointRequestOption += "waypoints=optimize:true" + "%7C" + geoPointAsString(herkenningsPoint2) + "%7C" + geoPointAsString(herkenningsPoint1) + "%7C" + geoPointAsString(herkenningsPoint3);
        roadManager.addRequestOption("mode=walking");
        Log.i("OSM", waypointRequestOption);
//        try {
//			waypointRequestOption = URLEncoder.encode(waypointRequestOption,"UTF-8"); 
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
        roadManager.addRequestOption(waypointRequestOption);
        Road road = roadManager.getRoad(waypoints);
        
        PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }
	
	   protected String geoPointAsString(GeoPoint p){
           StringBuffer result = new StringBuffer();
           double d = p.getLatitudeE6()*1E-6;
           result.append(Double.toString(d));
           d = p.getLongitudeE6()*1E-6;
           result.append("," + Double.toString(d));
           return result.toString();
   }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map_test, menu);
        return true;
    }
}
