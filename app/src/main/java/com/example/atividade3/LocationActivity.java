package com.example.atividade3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);

        Button btnStart = findViewById(R.id.button_start);
        Button btnStop = findViewById(R.id.button_stop);

        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdate();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdate();
            }
        });
    }

    public void startLocationUpdate() {

        if (locationCallback != null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {

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
                    Location location = locationResult.getLastLocation();

                    atualizaLocationTextView(location);
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
            );

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_UPDATES);
        }
    }

    private void stopLocationUpdate() {

        if (fusedLocationProviderClient != null && locationCallback != null) {

            fusedLocationProviderClient.removeLocationUpdates(locationCallback);


            Toast.makeText(this,
                    "Atualização parada",
                    Toast.LENGTH_SHORT).show();
            Log.d("GPS", "Atualizando localização");


            locationCallback = null;
        }
    }
    public void atualizaLocationTextView(Location location) {
        TextView locationTextView = findViewById(R.id.LocationText);

        String s = "Dados da Última Localização:\n";

        if (location != null) {
            s += "Latitude: " + location.getLatitude() + "\n";
            s += "Longitude: " + location.getLongitude() + "\n";
            s += "Altitude: " + location.getAltitude() + "\n";
            s += "Rumo (graus): " + location.getBearing() + "\n";
            s += "Velocidade (m/s): " + location.getSpeed() + "\n";
            s += "Precisão (m): " + location.getAccuracy() + "\n";
        }

        locationTextView.setText(s);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_UPDATES) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startLocationUpdate();

            } else {
                Toast.makeText(this,
                        "Sem permissão para mostrar atualizações da sua localização",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}