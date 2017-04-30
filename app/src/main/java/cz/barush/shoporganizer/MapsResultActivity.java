package cz.barush.shoporganizer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.barush.shoporganizer.persistance.entity.Supermarket;
import cz.barush.shoporganizer.services.AppController;
import cz.barush.shoporganizer.utils.Computation;
import cz.barush.shoporganizer.utils.DirectionsJSONParser;
import cz.barush.shoporganizer.utils.StaticPool;

import static cz.barush.shoporganizer.services.AppConfig.GEOMETRY;
import static cz.barush.shoporganizer.services.AppConfig.GOOGLE_BROWSER_API_KEY;
import static cz.barush.shoporganizer.services.AppConfig.ICON;
import static cz.barush.shoporganizer.services.AppConfig.LATITUDE;
import static cz.barush.shoporganizer.services.AppConfig.LOCATION;
import static cz.barush.shoporganizer.services.AppConfig.LONGITUDE;
import static cz.barush.shoporganizer.services.AppConfig.NAME;
import static cz.barush.shoporganizer.services.AppConfig.OK;
import static cz.barush.shoporganizer.services.AppConfig.PLACE_ID;
import static cz.barush.shoporganizer.services.AppConfig.PLAY_SERVICES_RESOLUTION_REQUEST;
import static cz.barush.shoporganizer.services.AppConfig.REFERENCE;
import static cz.barush.shoporganizer.services.AppConfig.STATUS;
import static cz.barush.shoporganizer.services.AppConfig.SUPERMARKET_ID;
import static cz.barush.shoporganizer.services.AppConfig.TAG;
import static cz.barush.shoporganizer.services.AppConfig.VICINITY;
import static cz.barush.shoporganizer.services.AppConfig.ZERO_RESULTS;

public class MapsResultActivity extends AppCompatActivity implements OnMapReadyCallback
{
    LocationManager locationManager;
    CoordinatorLayout mainCoordinatorLayout;
    Location myCurrentLocation;
    //PRINT
    ArrayList<LatLng> markerPoints;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mapsresult);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mainCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.mainCoordinatorLayout);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        showCurrentLocation();
    }

    private void showCurrentLocation()
    {
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        myCurrentLocation = locationManager.getLastKnownLocation(bestProvider);

        if (myCurrentLocation == null)
        {
            Toast.makeText(MapsResultActivity.this, "Go outside of the building, we couldn't find your location", Toast.LENGTH_LONG).show();
        }
        else
        {
            drawRoute(StaticPool.polylines);
        }
    }

    private void drawRoute(List<List<HashMap<String, String>>> result)
    {
        ArrayList<LatLng> points = null;
        PolylineOptions lineOptions = null;

        // Traversing through all the routes
        for (int i = 0; i < result.size(); i++)
        {
            mMap.clear();

            points = new ArrayList<LatLng>();
            lineOptions = new PolylineOptions();

            // Fetching i-th route
            List<HashMap<String, String>> path = result.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++)
            {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(2);
            lineOptions.color(Color.RED);
        }

        // Drawing polyline in the Google Map for the i-th route
        mMap.addPolyline(lineOptions);
    }
}