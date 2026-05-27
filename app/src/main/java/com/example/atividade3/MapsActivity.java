package com.example.atividade3;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;

    private static final int REQUEST_CONFIG = 1;

    private int tipoMapa = GoogleMap.MAP_TYPE_NORMAL;

    private boolean courseUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);

        // BOTÃO CONFIG
        Button btnconfig =
                findViewById(R.id.button_config);

        btnconfig.setOnClickListener(this);

        // BOTÃO VOLTAR
        Button btnBack =
                findViewById(R.id.button_back);

        btnBack.setOnClickListener(v -> finish());

        // MAPA
        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getSupportFragmentManager()
                                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_config) {

            Intent i =
                    new Intent(this, Config.class);

            startActivityForResult(i, REQUEST_CONFIG);
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data);

        if (requestCode == REQUEST_CONFIG
                && resultCode == RESULT_OK) {

            tipoMapa = data.getIntExtra(
                    "tipoMapa",
                    GoogleMap.MAP_TYPE_NORMAL);

            courseUp = data.getBooleanExtra(
                    "courseUp",
                    false);

            atualizarMapa();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        atualizarMapa();
    }

    private void atualizarMapa() {

        if (mMap == null) return;

        mMap.clear();

        mMap.setMapType(tipoMapa);

        LatLng ucsal =
                new LatLng(-12.94825, -38.41334);

        float bearing;

        // COURSE UP
        if (courseUp) {
            bearing = 180;
        }

        // NORTH UP
        else {
            bearing = 0;
        }

        CameraPosition cameraPosition =
                new CameraPosition.Builder()
                        .target(ucsal)
                        .zoom(18)
                        .tilt(45)
                        .bearing(bearing)
                        .build();

        mMap.animateCamera(
                CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

        mMap.addMarker(
                new MarkerOptions()
                        .position(ucsal)
                        .title("UCSAL")
                        .snippet("Campus Pituaçu"));

        mMap.addCircle(
                new CircleOptions()
                        .center(ucsal)
                        .radius(200)
                        .strokeWidth(5)
                        .strokeColor(Color.RED)
                        .fillColor(0x30FF0000));
    }
}