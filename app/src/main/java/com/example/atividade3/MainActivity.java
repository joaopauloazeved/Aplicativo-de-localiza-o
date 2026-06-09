package com.example.atividade3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

        TextView textView   = findViewById(R.id.textView);
        Button btnRegistrar = findViewById(R.id.button_registrar);
        Button btnConsultar = findViewById(R.id.button_consultar);

        btnRegistrar.setOnClickListener(this);
        btnConsultar.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.button_registrar) {
            startActivity(new Intent(this, RegistrarTrilhaActivity.class));
        } else if (id == R.id.button_consultar) {
            startActivity(new Intent(this, ConsultarTrilhasActivity.class));
        }
    }
}