package com.example.wera.ciscoapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.example.wera.ciscoapp.util.DownloadFileTask;
import com.example.wera.ciscoapp.util.FileManager;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EventDownloader {

    private ArrayList<MapEntity> markerList;
    private Context context;
    private int eventCount, timeCount;

    public EventDownloader(Context context, String address, int meterRadius) {
        this.context = context;
        markerList = new ArrayList<>();

        LatLng location = getLocationFromAddress(context, address);

        getAllEvents(location, meterRadius);
    }

    /**
     * Converts the address string provided by Google Calendar to latitude & longitude coordinates.
     */
    public static LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
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

    private void getAllEvents(final LatLng latLng, int radiusInMeters) {
        new DownloadFileTask(context, new DownloadFileTask.DownloadOptions()
                .setFileName("foods_" + System.nanoTime() + ".json")
                .setFilePath(context.getCacheDir().getPath())
                .setOverwrite(true)
        ) {
            @Override
            public void onPostExecute(File file) {
                if (file != null) {
                    try {
                        String jsonString = FileManager.readTextFile(file);

                        JSONArray results = new JSONObject(jsonString).getJSONArray("results");
                        if (results != null) {
                            eventCount = results.length();
                            for (int i = 0; i < results.length(); i++) {

                                JSONObject child = results.getJSONObject(i);
                                if (child != null) {
                                    MapEntity entity = new MapEntity();

                                    entity.name = child.getString("name");

                                    JSONObject location = child.getJSONObject("geometry").getJSONObject("location");
                                    entity.latLng = new LatLng(location.getDouble("lat"), location.getDouble("lng"));

                                    markerList.add(entity);

                                    getDistance(latLng, markerList.get(i));
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.execute(
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                        "location=" + latLng.latitude + "," + latLng.longitude +
                        "&radius=" + radiusInMeters +
                        "&types=food" +
                        "&key=AIzaSyAEDhokgRN8ilwIGymUO9YkAo03UEBYlTY"
        );
    }

    private void getDistance(LatLng fromLatLng, final MapEntity entity) {
        new DownloadFileTask(context, new DownloadFileTask.DownloadOptions()
                .setFileName("distance_" + System.nanoTime() + ".json")
                .setFilePath(context.getCacheDir().getPath())
                .setOverwrite(true)
        ) {
            @Override
            public void onPostExecute(File file) {
                if (file != null) {
                    try {
                        String jsonString = FileManager.readTextFile(file);

                        JSONObject result = new JSONObject(jsonString).getJSONArray("rows").getJSONObject(0);
                        if (result != null) {
                            JSONObject duration = result.getJSONArray("elements").getJSONObject(0).getJSONObject("duration");
                            entity.time = duration.getString("text");
                            timeCount++;
                            Log.i("TIMECOUNT", timeCount + "/" + eventCount);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        if (timeCount == eventCount) {
                            onLoadComplete(markerList);
                        }
                    }
                }
            }
        }.execute(
                "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                "origins=" + fromLatLng.latitude + "," + fromLatLng.longitude +
                "&destinations=" + entity.latLng.latitude + "," + entity.latLng.longitude +
                "&mode=walking" +
                "&language=en-EN" +
                "&key=AIzaSyAEDhokgRN8ilwIGymUO9YkAo03UEBYlTY"
        );
    }

    public void onLoadComplete(ArrayList<MapEntity> entityList) {

    }
}
