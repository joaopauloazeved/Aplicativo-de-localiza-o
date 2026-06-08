package com.example.atividade3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Locale;

public class RegistrarTrilhaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedClient;

    private boolean modoSimulacao = false;
    private boolean rastreando = false;
    private Handler simulacaoHandler = new Handler();
    private Runnable simulacaoRunnable;
    private int simIndex = 0;

    private Location lastLocation;

    private TextView tvStatus, tvVelocidade, tvDistancia, tvCronometro;
    private Button btnIniciar, btnParar, btnSimular;

    private long tempoInicio = 0;
    private Handler cronometroHandler = new Handler();
    private Runnable cronometroRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_trilha);

        tvStatus = findViewById(R.id.tv_status);
        tvVelocidade = findViewById(R.id.tv_velocidade);
        tvDistancia = findViewById(R.id.tv_distancia);
        tvCronometro = findViewById(R.id.tv_cronometro);

        btnIniciar = findViewById(R.id.btn_iniciar);
        btnParar = findViewById(R.id.btn_parar);
        btnSimular = findViewById(R.id.btn_simular);

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        btnSimular.setOnClickListener(v -> {
            modoSimulacao = !modoSimulacao;

            if (modoSimulacao) {
                btnSimular.setText("SIMULAÇÃO: ON");
                btnSimular.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32"))
                );
                Toast.makeText(this, "Modo simulação ativado. Clique em INICIAR.", Toast.LENGTH_SHORT).show();
            } else {
                btnSimular.setText("MODO SIMULAÇÃO");
                btnSimular.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#E65100"))
                );
            }
        });

        btnIniciar.setOnClickListener(v -> iniciarTrilha());
        btnParar.setOnClickListener(v -> pararTrilha());
    }

    private void iniciarTrilha() {
        if (rastreando) return;

        rastreando = true;
        tempoInicio = SystemClock.elapsedRealtime();

        tvStatus.setText("GRAVANDO");
        btnIniciar.setEnabled(false);
        btnParar.setEnabled(true);
        btnSimular.setEnabled(false);

        iniciarCronometro();

        if (modoSimulacao) iniciarSimulacao();
    }

    private void pararTrilha() {
        if (!rastreando) return;

        rastreando = false;

        pararSimulacao();
        pararCronometro();

        tvStatus.setText("PARADO");
        btnIniciar.setEnabled(true);
        btnParar.setEnabled(false);
        btnSimular.setEnabled(true);
    }

    private void iniciarSimulacao() {
        simulacaoRunnable = new Runnable() {
            @Override
            public void run() {
                if (!rastreando) return;

                Location loc = new Location("simulada");
                loc.setLatitude(-12.97 + simIndex * 0.0001);
                loc.setLongitude(-38.50 + simIndex * 0.0001);

                if (simIndex > 0 && lastLocation != null) {
                    float dist = lastLocation.distanceTo(loc);
                    loc.setSpeed(dist / 2.0f);
                } else {
                    loc.setSpeed(3.5f);
                }

                lastLocation = loc;
                simIndex++;

                double velocidade = loc.getSpeed() * 3.6;
                tvVelocidade.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidade));
                tvDistancia.setText(String.format(Locale.getDefault(), "%d m", simIndex * 10));

                simulacaoHandler.postDelayed(this, 2000);
            }
        };
        simulacaoHandler.post(simulacaoRunnable);
    }

    private void pararSimulacao() {
        if (simulacaoRunnable != null) {
            simulacaoHandler.removeCallbacks(simulacaoRunnable);
        }
    }

    private void iniciarCronometro() {
        cronometroRunnable = new Runnable() {
            @Override
            public void run() {
                long tempo = SystemClock.elapsedRealtime() - tempoInicio;
                long s = tempo / 1000;
                tvCronometro.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", s / 3600, (s % 3600) / 60, s % 60));
                cronometroHandler.postDelayed(this, 1000);
            }
        };
        cronometroHandler.post(cronometroRunnable);
    }

    private void pararCronometro() {
        if (cronometroRunnable != null) {
            cronometroHandler.removeCallbacks(cronometroRunnable);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }
}
