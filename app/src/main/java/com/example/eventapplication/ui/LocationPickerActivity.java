package com.example.eventapplication.ui;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eventapplication.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity {

    private MapView mapView;
    private GeoPoint selectedPoint;
    private Marker marker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required by osmdroid: set a user agent to avoid getting blocked
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_location_picker);

        mapView = findViewById(R.id.mapView);
        Button btnConfirm = findViewById(R.id.btnConfirmLocation);

        mapView.setMultiTouchControls(true);

        // Center map somewhere reasonable (example: Tunis)
        IMapController controller = mapView.getController();
        controller.setZoom(12.0);
        GeoPoint startPoint = new GeoPoint(36.8065, 10.1815);
        controller.setCenter(startPoint);

        // Listen for taps to select location
        MapEventsReceiver receiver = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                setSelectedPoint(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                setSelectedPoint(p);
                return true;
            }
        };
        MapEventsOverlay overlay = new MapEventsOverlay(receiver);
        mapView.getOverlays().add(overlay);

        btnConfirm.setOnClickListener(v -> {
            if (selectedPoint == null) {
                Toast.makeText(this, "Tap on the map to choose a location", Toast.LENGTH_SHORT).show();
                return;
            }

            double lat = selectedPoint.getLatitude();
            double lng = selectedPoint.getLongitude();
            String address = reverseGeocode(lat, lng);

            Intent data = new Intent();
            data.putExtra("lat", lat);
            data.putExtra("lng", lng);
            data.putExtra("address", address);
            setResult(RESULT_OK, data);
            finish();
        });
    }

    private void setSelectedPoint(GeoPoint p) {
        selectedPoint = p;
        if (marker == null) {
            marker = new Marker(mapView);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(marker);
        }
        marker.setPosition(p);
        mapView.getController().animateTo(p);
        mapView.invalidate();
    }

    private String reverseGeocode(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address a = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= a.getMaxAddressLineIndex(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(a.getAddressLine(i));
                }
                return sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
