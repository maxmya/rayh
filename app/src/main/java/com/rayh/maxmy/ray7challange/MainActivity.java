package com.rayh.maxmy.ray7challange;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, LocationListener, View.OnClickListener {


    // Map Components
    private GoogleMap mMap;
    private Marker fromMarker;
    private Marker toMarker;
    private Polyline lines;

    // View Components
    private Button pickMyCar;
    private Button requestPickup;
    private ImageView showWay;
    private View autocompleteFromCancelBtn;
    private View autocompleteToCancelBtn;

    // Fragments
    private PlaceAutocompleteFragment autocompleteFrom;
    private PlaceAutocompleteFragment autocompleteTo;
    private SupportMapFragment mapFragment;

    // Location Objects
    private LocationManager locationManager;
    private Location currentLocation;

    // API Const data
    private static final String ADDRESS_API = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";
    private final String DIRECTIONS_API = "https://maps.googleapis.com/maps/api/distancematrix/json?units=metric";
    private final String DIRECTIONS_KEY = "AIzaSyBjWyXbMRXKHcqniTDAhYMkmtXU-zXek1I";

    //UI Elements
    private ProgressDialog progressDialog;
    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // map fragment initialization
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // toolbar initialization
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // drawer initialization
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // autocomplete fragment From
        autocompleteFrom = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.from_autocomplete);
        autocompleteFrom.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (fromMarker != null)
                    fromMarker.remove();

                fromMarker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng()).title("from " + place.getAddress().toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fromMarker.getPosition(), 10));
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(MainActivity.this, "Error :" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        ((EditText) autocompleteFrom.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Trip Starting Point");

        autocompleteFromCancelBtn = autocompleteFrom.getView().findViewById(R.id.place_autocomplete_clear_button);
        autocompleteFromCancelBtn.setOnClickListener(this);

        // autocomplete fragment To
        autocompleteTo = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.to_autocomplete);
        autocompleteTo.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (toMarker != null)
                    toMarker.remove();

                toMarker = mMap.addMarker(new MarkerOptions()
                        .position(place.getLatLng()).title("to " + place.getAddress().toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                );

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(toMarker.getPosition(), 10));

            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "Error :" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        ((EditText) autocompleteTo.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Ending Point");

        autocompleteToCancelBtn = autocompleteTo.getView().findViewById(R.id.place_autocomplete_clear_button);
        autocompleteToCancelBtn.setOnClickListener(this);

        // UI buttons referencing
        pickMyCar = (Button) findViewById(R.id.takeMyCarBtn);
        requestPickup = (Button) findViewById(R.id.requestPickupBtn);
        showWay = (ImageView) findViewById(R.id.showWay);

        // UI listeners initialization
        pickMyCar.setOnClickListener(this);
        requestPickup.setOnClickListener(this);
        showWay.setOnClickListener(this);

        // progress dialog for drawing rout
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Getting Rout Info ..");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();


        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
        } else {
        }

        // move myLocation button to bottom right
        View lb = ((View) mapFragment.getView().findViewById(1).getParent()).findViewById(2);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                lb.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParams.setMargins(0, 0, 30, 30);

        // move camera to egypt
        LatLng egypt = new LatLng(30.0444, 31.2357);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(egypt, 10.0f));
    }

    @Override
    public void onMapLongClick(LatLng point) {

        if (toMarker != null)
            toMarker.remove();


        toMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        getAddress(point, autocompleteTo);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(toMarker.getPosition(), 10));

    }


    @Override
    public boolean onMyLocationButtonClick() {


        currentLocation = getLastKnownLocation();

        if (fromMarker != null)
            fromMarker.remove();

        fromMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .title("Current Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        );

        getAddress(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), autocompleteFrom);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(fromMarker.getPosition(), 10));

        return true;
    }

    // get current location from last assigned data in device
    private Location getLastKnownLocation() {

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                continue;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }

        return bestLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    // pass fragment by reference to change it from async task
    public void getAddress(LatLng point, PlaceAutocompleteFragment view) {
        String lat = String.valueOf(point.latitude);
        String lng = String.valueOf(point.longitude);
        // use async task to get json from locations api
        GetAddressTask mTask = new GetAddressTask(MainActivity.this, view);
        mTask.execute(ADDRESS_API + lat + "," + lng + "&sensor=true");
    }

    @Override
    public void onClick(View v) {

        // top right button to show way without request
        if (v == showWay) {

            showWayRout();

        } else if (v == autocompleteToCancelBtn) {

            toMarker.remove();
            toMarker = null;

            if (lines != null)
                lines.remove();

            autocompleteTo.setText("");

        } else if (v == autocompleteFromCancelBtn) {

            fromMarker.remove();
            fromMarker = null;

            if (lines != null)
                lines.remove();

            autocompleteFrom.setText(null);

        } else if (v == pickMyCar || v == requestPickup) { // same action , better to separate bs when they changed

            requestClick();

        }
    }

    // preform action of click bring time date picker and draw rout
    public void requestClick() {
        if (fromMarker != null && toMarker != null) {
            showWayRout();
            openTimeDateDialog();
        } else {
            Toast.makeText(this, "Choose Source & Destination First", Toast.LENGTH_SHORT).show();
        }
    }

    // open alert dialog with time and date without using date or time !
    public void openTimeDateDialog() {
        final View dialogView = View.inflate(this, R.layout.time_data_dialog, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }

    // draw rout from source to destination
    public void showWayRout() {

        if (lines != null)
            lines.remove();

        if (fromMarker == null || toMarker == null) {
            Toast.makeText(this, "Select Source and Destination First", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origin = fromMarker.getPosition();
        LatLng dest = toMarker.getPosition();
        String url = getDirectionsUrl(origin, dest);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);

        LatLngBounds bounds = new LatLngBounds.Builder().include(origin).include(dest).build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 10));

    }


    // open source methods to parse directions and draw rout , i made some changes to sut my app
    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();
            String distance = "";
            String duration = "";

            if (result.size() < 1) {
                Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if (j == 0) {    // Get distance from the list
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) { // Get duration from the list
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
            }


            // Drawing polyline in the Google Map for the i-th route
            lines = mMap.addPolyline(lineOptions);
            progressDialog.dismiss();

        }


    }


}


