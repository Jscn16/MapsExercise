package fi.jamk.mapstest1;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = "MapsApp";
    private static final String SERVICE_URL = "https://api.myjson.com/bins/2my3m";
    private GoogleMap map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }


    private void setUpMapIfNeeded() {
        if (map == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {

                @Override
                public void onMapReady(GoogleMap googlemap) {
                    map = googlemap;
                    if (map != null) {
                        setUpMap();
                        LatLng jyv = new LatLng(62.243197, 25.751427);
                        CameraPosition target = CameraPosition.builder().target(jyv).zoom(15).build();
                        map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                    }
                }
            });
        }
    }


    private void setUpMap() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    getAndAddCity();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Cannot retrive cities", e);
                    return;
                }
            }
        }).start();
    }


    protected void getAndAddCity() throws IOException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to service", e);
            throw new IOException("Error connecting to service", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }


        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    addMarkersWithJson(json.toString());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing JSON", e);
                }
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    getAndAddCity();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Cannot retrive cities", e);
                    return;
                }
            }
        }).start();
    }


    void addMarkersWithJson(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            map.addMarker(new MarkerOptions()
                    .title(jsonObj.getString("name"))
                    .position(new LatLng(
                            jsonObj.getJSONArray("latlng").getDouble(0),
                            jsonObj.getJSONArray("latlng").getDouble(1)
                    ))
            );
        }
    }
}