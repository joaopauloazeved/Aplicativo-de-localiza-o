package com.example.atividade3;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    // Código da requisição
    private static final int REQUEST_CONFIG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Botão Config
        Button btnconfig = findViewById(R.id.button_config);
        btnconfig.setOnClickListener(this);

        // Inicializa o mapa
        SupportMapFragment mapFragment =
                (SupportMapFragment)
                        getSupportFragmentManager()
                                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    // Clique do botão
    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.button_config) {

            Intent i = new Intent(
                    this,
                    Config.class);

            // Abre Config esperando resultado
            startActivityForResult(i, REQUEST_CONFIG);
        }
    }

    // Recebe o resultado da Config
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data);

        if (requestCode == REQUEST_CONFIG
                && resultCode == RESULT_OK) {

            int tipoMapa = data.getIntExtra(
                    "tipoMapa",
                    GoogleMap.MAP_TYPE_NORMAL);

            // Muda o tipo do mapa
            mMap.setMapType(tipoMapa);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Coordenadas da UCSAL
        LatLng ucsal =
                new LatLng(-12.94825, -38.41334);

        // CameraPosition
        CameraPosition cameraPosition =
                new CameraPosition(
                        ucsal,
                        18,
                        45,
                        90
                );

        // Marcador UCSAL
        mMap.addMarker(new MarkerOptions()
                .position(ucsal)
                .title("UCSAL")
                .snippet("Campus Pituaçu"));

        // Marcador Salvador
        LatLng salvador =
                new LatLng(-12.9714, -38.5014);

        mMap.addMarker(new MarkerOptions()
                .position(salvador)
                .title("Salvador - BA"));

        // Anima câmera
        mMap.animateCamera(
                CameraUpdateFactory
                        .newCameraPosition(cameraPosition));

        // Círculo
        mMap.addCircle(new CircleOptions()
                .center(ucsal)
                .radius(200)
                .strokeWidth(5)
                .strokeColor(Color.RED)
                .fillColor(0x30FF0000));
    }
}