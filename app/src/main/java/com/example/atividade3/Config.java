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

    Button btnNorthUp;
    Button btnCourseUp;

    int tipoMapa = GoogleMap.MAP_TYPE_NORMAL;
    boolean courseUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_config);

        btnVetorial = findViewById(R.id.btnVetorial);
        btnSatelite = findViewById(R.id.btnSatelite);

        btnNorthUp = findViewById(R.id.btnNorthUp);
        btnCourseUp = findViewById(R.id.btnCourseUp);

        btnVetorial.setOnClickListener(v -> {
            tipoMapa = GoogleMap.MAP_TYPE_NORMAL;
        });

        btnSatelite.setOnClickListener(v -> {
            tipoMapa = GoogleMap.MAP_TYPE_SATELLITE;
        });

        btnNorthUp.setOnClickListener(v -> {
            courseUp = false;
        });

        btnCourseUp.setOnClickListener(v -> {
            courseUp = true;
        });

        Button btnSalvar = findViewById(R.id.btnSalvar);

        btnSalvar.setOnClickListener(v -> {

            Intent intent = new Intent();

            intent.putExtra("tipoMapa", tipoMapa);

            intent.putExtra("courseUp", courseUp);

            setResult(RESULT_OK, intent);

            finish();
        });
    }
}