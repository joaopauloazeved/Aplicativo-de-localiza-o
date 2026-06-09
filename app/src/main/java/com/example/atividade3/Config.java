package com.example.atividade3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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

    private static final String PREFS_NAME = "app_config";
    private static final String KEY_MAP_TYPE = "tipo_mapa";
    private static final String KEY_COURSE_UP = "course_up";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_config);

        // Carregar configurações salvas
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        tipoMapa = prefs.getInt(KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
        courseUp = prefs.getBoolean(KEY_COURSE_UP, false);

        btnVetorial = findViewById(R.id.btnVetorial);
        btnSatelite = findViewById(R.id.btnSatelite);
        btnNorthUp = findViewById(R.id.btnNorthUp);
        btnCourseUp = findViewById(R.id.btnCourseUp);

        btnVetorial.setOnClickListener(v -> tipoMapa = GoogleMap.MAP_TYPE_NORMAL);
        btnSatelite.setOnClickListener(v -> tipoMapa = GoogleMap.MAP_TYPE_SATELLITE);
        btnNorthUp.setOnClickListener(v -> courseUp = false);
        btnCourseUp.setOnClickListener(v -> courseUp = true);

        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(v -> salvarConfiguracoes());
    }

    private void salvarConfiguracoes() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_MAP_TYPE, tipoMapa);
        editor.putBoolean(KEY_COURSE_UP, courseUp);
        editor.apply();


        Intent intent = new Intent();
        intent.putExtra("tipoMapa", tipoMapa);
        intent.putExtra("courseUp", courseUp);
        setResult(RESULT_OK, intent);

        Toast.makeText(this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
        finish();
    }
}