package eu.uniek.osmbonuspacktest;

import java.util.ArrayList;

import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.PathOverlay;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MapTestActivity extends Activity {

    private MapView map;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_test);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        GeoPoint startPoint = new GeoPoint(48.13, -1.63);
        MapController mapController = map.getController();
        mapController.setCenter(startPoint);
        mapController.setZoom(9);
        
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        waypoints.add(startPoint);
        waypoints.add(new GeoPoint(48.4, -2.0));
        waypoints.add(new GeoPoint(48.4, -1.9)); //end point
        
        RoadManager roadManager = new GoogleRoadManager();
        roadManager.addRequestOption("waypoints=optimize:true"); 
        roadManager.addRequestOption("sensor=false");
        roadManager.addRequestOption("mode=walking");
        Road road = roadManager.getRoad(waypoints);
        
        PathOverlay roadOverlay = RoadManager.buildRoadOverlay(road, map.getContext());
        map.getOverlays().add(roadOverlay);
        map.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map_test, menu);
        return true;
    }
}
