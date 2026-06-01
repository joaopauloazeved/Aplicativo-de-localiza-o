package com.example.atividade3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        TextView textView        = findViewById(R.id.textView);
        Button btnLocation       = findViewById(R.id.button_location);
        Button btnMaps           = findViewById(R.id.button_maps);
        Button btnRegistrar      = findViewById(R.id.button_registrar);   // NOVO
        Button btnConsultar      = findViewById(R.id.button_consultar);   // NOVO

        Animation fade  = AnimationUtils.loadAnimation(this, R.anim.suav);
        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);

        textView.startAnimation(fade);
        btnRegistrar.startAnimation(pulse);

        btnLocation.setOnClickListener(this);
        btnMaps.setOnClickListener(this);
        btnRegistrar.setOnClickListener(this);
        btnConsultar.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_location) {
            startActivity(new Intent(this, LocationActivity.class));
        } else if (id == R.id.button_maps) {
            startActivity(new Intent(this, MapsActivity.class));
        } else if (id == R.id.button_registrar) {
            startActivity(new Intent(this, RegistrarTrilhaActivity.class));
        } else if (id == R.id.button_consultar) {
            startActivity(new Intent(this, ConsultarTrilhasActivity.class));
        }
    }
}