package com.geniusforapp.testtracking.ui.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.geniusforapp.testtracking.R;
import com.geniusforapp.testtracking.utils.UiUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String TAG = TrackingActivity.class.getSimpleName();

    private static final String GEO_LOCATION = "geoLocation";
    private static final String DRIVER_LOCATION = "driver_location";

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;

    private Toolbar toolbar;
    private GoogleMap mMap;

    private GeoFire geoFire;

    Boolean startTracking;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        initViews();
        // init toolbar
        initActionBar();
        // init geo fire
        initGeoFire();
        // init google map
        initGoogleMap();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    private void initGoogleMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void initGeoFire() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(GEO_LOCATION)
                .child(FirebaseAuth.getInstance().getUid())
                .child(DRIVER_LOCATION)
                .child("l");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Double> location = (ArrayList<Double>) dataSnapshot.getValue();
                Log.d(TAG, "onDataChange: " + location.toString());
                routeMapLocation(new LatLng(location.get(0), location.get(1)));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
                .observeOn(Schedulers.newThread())
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


    // get current user location
    public void getLocation() {

    }

    // route Map
    @SuppressLint("MissingPermission")
    public void routeMapLocation(LatLng latLng) {
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setTrafficEnabled(true);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(UiUtils.getBitmapFromVectorDrawable(this, R.drawable.ic_car))));
            final CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)// Sets the center of the map to Mountain View
                    .zoom(15)// Sets the zoom
                    .bearing(90)// Sets the orientation of the camera to east
                    .tilt(30)// Sets the tilt of the camera to 30 degrees
                    .build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }


    /**
     * {@link Toast} show toast message
     *
     * @param message
     */
    private void showToast(String message) {
        Toast.makeText(TrackingActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);

    }
}
