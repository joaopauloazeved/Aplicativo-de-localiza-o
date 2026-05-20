package com.example.atividade3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;

public class Config extends AppCompatActivity {

    Button btnVetorial;
    Button btnSatelite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_config);

        // Dá opção de escolher entre dois tipos de mapa
        // (Vetorial ou Satélite)

        btnVetorial = findViewById(R.id.btnVetorial);
        btnSatelite = findViewById(R.id.btnSatelite);

        // MAPA NORMAL / VETORIAL
        btnVetorial.setOnClickListener(v -> {

            Intent intent = new Intent();

            intent.putExtra(
                    "tipoMapa",
                    GoogleMap.MAP_TYPE_NORMAL);

            setResult(RESULT_OK, intent);

            finish();
        });

        // MAPA SATÉLITE
        btnSatelite.setOnClickListener(v -> {

            Intent intent = new Intent();

            intent.putExtra(
                    "tipoMapa",
                    GoogleMap.MAP_TYPE_SATELLITE);

            setResult(RESULT_OK, intent);

            finish();
        });
    }
}