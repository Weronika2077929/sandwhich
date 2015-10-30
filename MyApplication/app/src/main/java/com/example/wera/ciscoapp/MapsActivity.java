package com.example.wera.ciscoapp;

import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.wera.ciscoapp.util.DownloadFileTask;
import com.example.wera.ciscoapp.util.FileManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String ORIGIN_KEY_LAT = "ORIGIN1";
    public static final String ORIGIN_KEY_LNG = "ORIGIN2";
    public static final String DEST_KEY_LAT = "DEST1";
    public static final String DEST_KEY_LNG = "DEST2";

    private GoogleMap map;
    private LatLng origin, dest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Bundle extras = getIntent().getExtras();
        origin = new LatLng(extras.getDouble(ORIGIN_KEY_LAT), extras.getDouble(ORIGIN_KEY_LNG));
        dest = new LatLng(extras.getDouble(DEST_KEY_LAT), extras.getDouble(DEST_KEY_LNG));

        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (origin != null && dest != null) {
            map.addMarker(new MarkerOptions().position(origin).title("ORIGIN"));
            map.addMarker(new MarkerOptions().position(dest).title("DESTINATION"));

            map.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                            .target(dest)
                            .zoom(13f)
                            .build()
            ));
        }
    }

    /**
     * Converts the address string provided by Google Calendar to latitude & longitude coordinates.
     */
    public LatLng getLocationFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng position;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            position = new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            );

            return position;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
