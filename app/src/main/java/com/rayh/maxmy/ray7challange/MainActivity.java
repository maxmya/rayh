package com.rayh.maxmy.ray7challange;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener, PlaceSelectionListener, GoogleMap.OnMyLocationButtonClickListener {


    private GoogleMap mMap;
    private DrawerLayout drawer;
    private Marker holdClickMarker;
    private PlaceAutocompleteFragment autocompleteFrom;
    private PlaceAutocompleteFragment autocompleteTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
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
        autocompleteFrom.setOnPlaceSelectedListener(this);
        ((EditText) autocompleteFrom.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Trip Starting Point");


        autocompleteTo = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.to_autocomplete);
        autocompleteTo.setOnPlaceSelectedListener(this);
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

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onMapLongClick(LatLng point) {

        if (holdClickMarker != null)
            holdClickMarker.remove();

        holdClickMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // convert LatLng to address and put it into the To autocomplete text view
        try {

            Geocoder geocoder = new Geocoder(this);
            if (geocoder == null) {
                Log.d("MEHERE", "a7a");

            }
            List<Address> addresses = geocoder.getFromLocation(point.longitude, point.latitude, 1);
            Log.d("MEHERE", addresses.toString());

            if (addresses != null && addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0) + " " + addresses.get(0).getAddressLine(1);
                if (address != null) {
                    ((EditText) autocompleteTo.getView().findViewById(R.id.place_autocomplete_search_input)).setText(address);

                } else {
                    ((EditText) autocompleteTo.getView().findViewById(R.id.place_autocomplete_search_input)).setText("No Address Available");
                }
            }

        } catch (Exception e) {
            Toast.makeText(this, "ERROR " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onPlaceSelected(Place place) {

    }

    @Override
    public void onError(Status status) {

    }

    @Override
    public boolean onMyLocationButtonClick() {



        return false;
    }
}
