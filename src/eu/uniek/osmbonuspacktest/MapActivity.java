package eu.uniek.osmbonuspacktest;

import java.util.ArrayList;

import org.osmdroid.bonuspack.routing.GoogleRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class MapActivity extends Activity implements SensorEventListener  {

    private TextView tv;
	private SensorManager mSensorManager;
	private Sensor mCompass;

	@Override
	protected void onResume() {
		 super.onResume();
		    mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);

	}
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map); 
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        RoadManager roadManager = new GoogleRoadManager();
        roadManager.addRequestOption("mode=walking");
        ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
        GeoPoint startPoint = new GeoPoint(52.068533, 5.089045);
        GeoPoint endPoint = new GeoPoint(52.210897,5.193325);
        waypoints.add(startPoint); 
        waypoints.add(endPoint);
        Road road = roadManager.getRoad(waypoints);
         tv = (TextView) findViewById(R.id.LOL); 
        tv.setText("");
        for(RoadNode s : road.mNodes) {
        	
        	tv.setText(tv.getText().toString() + s.mLocation.getLatitudeE6() + " | " + s.mLocation.getLongitudeE6() + " mtype" + s.mManeuverType + "\n\r");
        	
        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map, menu);
        return true;
    }

	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onSensorChanged(SensorEvent arg0) {
		tv.setText(""+ arg0.values[0]);
	}
}
