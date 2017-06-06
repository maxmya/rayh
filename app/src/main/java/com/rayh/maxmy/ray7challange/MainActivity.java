package com.rayh.maxmy.ray7challange;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMyLocationButtonClickListener, LocationListener {


    private GoogleMap mMap;
    private DrawerLayout drawer;
    private Marker fromMarker;
    private Marker toMarker;
    private PlaceAutocompleteFragment autocompleteFrom;
    private PlaceAutocompleteFragment autocompleteTo;
    private LocationManager locationManager;
    private Location currentLocation;
    private SupportMapFragment mapFragment;
    private static final String ADDRESS_API = "http://maps.googleapis.com/maps/api/geocode/json?latlng=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


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
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(MainActivity.this, "Error :" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();

            }
        });
        ((EditText) autocompleteFrom.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Trip Starting Point");


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
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MainActivity.this, "Error :" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        ((EditText) autocompleteTo.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Ending Point");


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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(egypt, 5.0f));
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
        return true;
    }

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
        DownloadTask mTask = new DownloadTask(MainActivity.this, view);
        mTask.execute(ADDRESS_API + lat + "," + lng + "&sensor=true");


    }


}


