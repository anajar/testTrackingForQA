package com.geniusforapp.testtracking.ui.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.geniusforapp.testtracking.R;
import com.geniusforapp.testtracking.ui.login.LoginActivity;
import com.geniusforapp.testtracking.ui.tracking.TrackingActivity;
import com.geniusforapp.testtracking.utils.UiUtils;
import com.github.florent37.rxgps.RxGps;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String GEO_LOCATION = "geoLocation";
    private static final String DRIVER_LOCATION = "driver_location";

    private GoogleMap mMap;

    private GeoFire geoFire;

    private Boolean startTracking = false;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private Toolbar toolbar;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        initViews();
        // init toolbar
        initActionBar();
        // init drawer
        initDrawer();
        // init geo fire
        initGeoFire();
        // init google map
        initGoogleMap();


    }

    private void initDrawer() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.prompt_drawer_open, R.string.prompt_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        // set email and display name
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.email)).setText(auth.getCurrentUser().getEmail());
        ((TextView) headerView.findViewById(R.id.display_name)).setText(auth.getCurrentUser().getDisplayName() == null ? "N/A" : auth.getCurrentUser().getDisplayName().isEmpty() ? "N/A" : auth.getCurrentUser().getDisplayName());
        auth.setLanguageCode(Locale.getDefault().getLanguage());
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_burger));

    }


    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        toolbar.animate().translationY(-100).setDuration(300).setStartDelay(300).start();
        toolbar.animate().translationY(0).setDuration(500).setStartDelay(500).start();
    }


    private void initGoogleMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        initMapStyle();
        checkPermission();
    }

    private void initMapStyle() {
        try {
            mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            Log.d(TAG, "initMapStyle: " + e.getLocalizedMessage());
        }
    }

    public void checkPermission() {
        new RxPermissions(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {
                            getLocation();
                        } else {
                            showToast(getString(R.string.toast_message_location));
                        }
                    }
                });
    }


    /**
     * {@link Toast} show toast message
     *
     * @param message
     */
    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    // get current user location
    public void getLocation() {
        new RxGps(this)
                .locationHight()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Location>() {
                    @Override
                    public void accept(Location location) throws Exception {
                        routeMapLocation(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (throwable instanceof RxGps.PermissionException) {
                            //the user does not allow the permission
                            showToast(getString(R.string.toast_message_location));
                        } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                            //the user do not have play services
                            showToast(getString(R.string.toast_message_play_service));
                        }
                    }
                });
    }

    // route Map
    @SuppressLint("MissingPermission")
    public void routeMapLocation(LatLng latLng) {
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(UiUtils.getBitmapFromVectorDrawable(this, R.drawable.ic_pin))));
            final CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)// Sets the center of the map to Mountain View
                    .zoom(15)// Sets the zoom
                    .bearing(90)// Sets the orientation of the camera to east
                    .tilt(30)// Sets the tilt of the camera to 30 degrees
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


            if (startTracking) {
                geoFire.setLocation(DRIVER_LOCATION, new GeoLocation(latLng.latitude, latLng.longitude));
            }
        }
    }


    public void initGeoFire() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GEO_LOCATION).child(FirebaseAuth.getInstance().getUid());
        geoFire = new GeoFire(ref);
    }


    public void onStartTracking(View view) {
        startTracking = !startTracking;
        // change the status button
        ((Button) view).setText(startTracking ? "Stop tracking" : "Start tracking");
        (view).setBackground(startTracking ? ContextCompat.getDrawable(this, R.drawable.ic_button_secondary) : ContextCompat.getDrawable(this, R.drawable.ic_button_primary));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        switch (item.getItemId()) {
            case R.id.action_track:
                startActivity(new Intent(this, MainActivity.class));
                break;
        }

        return true;
    }

}
