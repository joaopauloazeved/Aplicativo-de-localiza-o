package com.example.atividade3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_UPDATES = 1;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private DatabaseHelper databaseHelper;

    private long trilhaId;

    private double velocidadeMaxima = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_location);

        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.activity_location);

        Button btnStart = findViewById(R.id.button_start);
        Button btnStop = findViewById(R.id.button_stop);

        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        // BOTÃO INICIAR
        btnStart.setOnClickListener(v -> startLocationUpdate());

        // BOTÃO PARAR
        btnStop.setOnClickListener(v -> stopLocationUpdate());

        trilhaId = databaseHelper.inserirTrilha(
                "Minha Trilha",
                String.valueOf(System.currentTimeMillis()),
                "",
                0,
                0,
                0,
                ""
        );
    }

    public void startLocationUpdate() {

        // Evita múltiplos rastreamentos
        stopOnlyTracking();

        // Verifica permissão
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            long timeInterval = 3000;

            LocationRequest locationRequest =
                    new LocationRequest.Builder(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            timeInterval
                    ).build();

            locationCallback = new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult locationResult) {

                    super.onLocationResult(locationResult);

                    Location location =
                            locationResult.getLastLocation();
                    double velocidadeAtual =
                            location.getSpeed() * 3.6;

                    if (velocidadeAtual > velocidadeMaxima) {

                        velocidadeMaxima = velocidadeAtual;
                    }

                    // SALVA PONTO NO SQLITE
                    databaseHelper.inserirPonto(
                            trilhaId,
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    atualizaLocationTextView(location);
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
            );

            Toast.makeText(
                    this,
                    "Rastreamento iniciado",
                    Toast.LENGTH_SHORT
            ).show();

            Log.d("GPS", "Atualização iniciada");

        } else {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION_UPDATES
            );
        }
    }

    // PARA APENAS O GPS
    private void stopOnlyTracking() {

        if (locationCallback != null) {

            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            locationCallback = null;
        }
    }

    // PARA GPS E VOLTA PARA MAIN
    private void stopLocationUpdate() {

        stopOnlyTracking();

        Toast.makeText(
                this,
                "Rastreamento parado",
                Toast.LENGTH_SHORT
        ).show();

        Log.d("GPS", "Atualização parada");

        // Volta para MainActivity
        finish();
    }

    public void atualizaLocationTextView(Location location) {

        TextView locationTextView =
                findViewById(R.id.LocationText);

        if (location != null) {

            float velocidade =
                    location.getSpeed() * 3.6f;

            String s =
                    "Latitude:\n"
                            + location.getLatitude()

                            + "\n\nLongitude:\n"
                            + location.getLongitude()

                            + "\n\nVelocidade:\n"
                            + String.format("%.2f", velocidade)
                            + " km/h"

                            + "\n\nPrecisão:\n"
                            + String.format("%.2f",
                            location.getAccuracy())
                            + " metros"

                            + "\n\nAltitude:\n"
                            + String.format("%.2f",
                            location.getAltitude())
                            + " m"

                            + "\n\nRumo:\n"
                            + String.format("%.2f",
                            location.getBearing())
                            + "°";

            locationTextView.setText(s);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_LOCATION_UPDATES) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                startLocationUpdate();

            } else {

                Toast.makeText(
                        this,
                        "Permissão negada",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        stopOnlyTracking();
    }
}