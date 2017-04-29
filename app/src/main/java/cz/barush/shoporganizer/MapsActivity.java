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

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import gurobi.GRBException;

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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener
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

        if (!isGooglePlayServicesAvailable())
        {
            return;
        }
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mainCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.mainCoordinatorLayout);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            showLocationSettings();
        }
    }

    private void showLocationSettings()
    {
        Snackbar snackbar = Snackbar
                .make(mainCoordinatorLayout, "Location Error: GPS Disabled!",
                        Snackbar.LENGTH_LONG)
                .setAction("Enable", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                });
        snackbar.setActionTextColor(Color.RED);
        snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView
                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        snackbar.show();
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
            Toast.makeText(MapsActivity.this, "Go outside of the building, we couldn't find your location", Toast.LENGTH_LONG).show();
        } else loadNearByPlaces(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude());
    }

    private void loadNearByPlaces(double latitude, double longitude)
    {
        //YOU Can change this type at your own will, e.g hospital, cafe, restaurant.... and see how it all works
        String type = "grocery_or_supermarket";
        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
        googlePlacesUrl.append("&radius=").append(StaticPool.user.getMaxRadius());
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);

        JsonObjectRequest request = new JsonObjectRequest(googlePlacesUrl.toString(),
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject result)
                    {
                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try
                        {
                            List<Supermarket> foundSupermarkets = parseLocationResult(result);
                            List<Integer> bestCombination = Computation.getBestCombination(foundSupermarkets, myCurrentLocation);
                            printRouteWithBestCombination(foundSupermarkets, bestCombination);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                        catch (GRBException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });

        AppController.getInstance().addToRequestQueue(request);
    }

    private List<Supermarket> parseLocationResult(JSONObject result) throws JSONException
    {
        List<Supermarket> foundSupermarkets = new ArrayList<>();
        String id, place_id, placeName = null, reference, icon, vicinity = null;
        double latitude, longitude;

        try
        {
            JSONArray jsonArray = result.getJSONArray("results");

            if (result.getString(STATUS).equalsIgnoreCase(OK))
            {
                mMap.clear();

                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject place = jsonArray.getJSONObject(i);
                    id = place.getString(SUPERMARKET_ID);
                    place_id = place.getString(PLACE_ID);
                    if (!place.isNull(NAME))
                    {
                        placeName = place.getString(NAME);
                        //ELIMINATE STORES SUCH AS DM DROGERY, ZABKA ETC.
                        boolean isInTheListOfSupermarkets = false;
                        for (Supermarket.SupermarketType st : Supermarket.SupermarketType.values())
                            if (placeName.contains(st.getName())) isInTheListOfSupermarkets = true;
                        if (!isInTheListOfSupermarkets) continue;
                    }
                    if (!place.isNull(VICINITY))
                    {
                        vicinity = place.getString(VICINITY);
                    }
                    latitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getDouble(LATITUDE);
                    longitude = place.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getDouble(LONGITUDE);
                    reference = place.getString(REFERENCE);
                    icon = place.getString(ICON);

                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(latitude, longitude);
                    markerOptions.position(latLng);
                    markerOptions.title(placeName + " : " + vicinity);

                    mMap.addMarker(markerOptions);
                    Location newLocation = new Location("LocationProvider");
                    newLocation.setLatitude(latitude);
                    newLocation.setLongitude(longitude);
                    foundSupermarkets.add(new Supermarket()
                            .setName(placeName)
                            .setLocation(newLocation)
                            .setPriceList(StaticPool.initializePriceList()));
                }

                Toast.makeText(getBaseContext(), foundSupermarkets.size() + " Supermarkets found!",
                        Toast.LENGTH_LONG).show();
            } else if (result.getString(STATUS).equalsIgnoreCase(ZERO_RESULTS))
            {
                Toast.makeText(getBaseContext(), "No Supermarket found in given radius!!!",
                        Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e)
        {
            e.printStackTrace();
            Log.e(TAG, "parseLocationResult: Error=" + e.getMessage());
        }
        return foundSupermarkets;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(latLng).title("My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        loadNearByPlaces(latitude, longitude);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {
    }

    @Override
    public void onProviderEnabled(String s)
    {
    }

    @Override
    public void onProviderDisabled(String s)
    {
    }

    private boolean isGooglePlayServicesAvailable()
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (apiAvailability.isUserResolvableError(resultCode))
            {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else
            {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    //PRINTING THE ROUTE
    private void printRouteWithBestCombination(List<Integer> bestCombination, List<Supermarket> foundSupermarkets)
    {
        LatLng origin = new LatLng(myCurrentLocation.getLatitude(), myCurrentLocation.getLongitude());
        LatLng dest = new LatLng(StaticPool.user.getHomeLocation().getLatitude(), StaticPool.user.getHomeLocation().getLongitude());
        markerPoints = new ArrayList<>();
        for (int i = 0; i < bestCombination.size(); i++)
        {
            Location loc = bestCombination.get(i).getLocation();
            markerPoints.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }

        // Getting URL to the Google Directions API
        String url = Computation.getDirectionsUrl(origin, dest, markerPoints);
        JsonObjectRequest request = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject result)
                    {
                        Log.i(TAG, "onResponse: Result= " + result.toString());
                        try
                        {
                            List<List<HashMap<String, String>>> routes = parseDirectionsResult(result);
                            drawRoute(routes);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e(TAG, "onErrorResponse: Error= " + error);
                        Log.e(TAG, "onErrorResponse: Error= " + error.getMessage());
                    }
                });
        AppController.getInstance().addToRequestQueue(request);
    }


    private List<List<HashMap<String, String>>> parseDirectionsResult(JSONObject result) throws JSONException
    {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try
        {
            DirectionsJSONParser parser = new DirectionsJSONParser();
            routes = parser.parse(result);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return routes;
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