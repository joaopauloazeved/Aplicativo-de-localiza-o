package com.example.atividade3;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Locale;

public class VisualizarTrilhaActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper db;

    private int trilhaId;
    private String nome, dataInicio, dataFim, duracao;
    private double velMedia, velMax, distancia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizar_trilha);

        db = new DatabaseHelper(this);

        // Recebe dados da trilha
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            trilhaId   = extras.getInt("trilha_id");
            nome       = extras.getString("nome", "Trilha");
            dataInicio = extras.getString("dataInicio", "-");
            dataFim    = extras.getString("dataFim", "-");
            velMedia   = extras.getDouble("velMedia", 0);
            velMax     = extras.getDouble("velMax", 0);
            distancia  = extras.getDouble("distancia", 0);
            duracao    = extras.getString("duracao", "-");
        }

        // Preenche overlay de estatísticas
        ((TextView) findViewById(R.id.tv_vis_nome)).setText(nome);
        ((TextView) findViewById(R.id.tv_vis_data_inicio)).setText("Início: " + dataInicio);
        ((TextView) findViewById(R.id.tv_vis_data_fim)).setText("Fim: " + dataFim);
        ((TextView) findViewById(R.id.tv_vis_vel_media))
                .setText(String.format(Locale.getDefault(), "Vel. Média: %.1f km/h", velMedia));
        ((TextView) findViewById(R.id.tv_vis_vel_max))
                .setText(String.format(Locale.getDefault(), "Vel. Máx: %.1f km/h", velMax));
        ((TextView) findViewById(R.id.tv_vis_distancia))
                .setText(String.format(Locale.getDefault(), "Distância: %.2f km", distancia));
        ((TextView) findViewById(R.id.tv_vis_duracao)).setText("Duração: " + duracao);

        findViewById(R.id.btn_vis_voltar).setOnClickListener(v -> finish());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_visualizar);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        List<LatLng> pontos = db.listarPontosTrilha(trilhaId);

        if (pontos.isEmpty()) return;

        // Desenha polilinha do trajeto
        mMap.addPolyline(new PolylineOptions()
                .addAll(pontos)
                .color(Color.parseColor("#E53935"))
                .width(8));

        // Marcador de início (verde)
        mMap.addMarker(new MarkerOptions()
                .position(pontos.get(0))
                .title("Início")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Marcador de fim (vermelho)
        LatLng fim = pontos.get(pontos.size() - 1);
        mMap.addMarker(new MarkerOptions()
                .position(fim)
                .title("Fim")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Centraliza câmera para abranger todo o trajeto
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng p : pontos) builder.include(p);
        LatLngBounds bounds = builder.build();

        mMap.setOnMapLoadedCallback(() ->
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        );
    }
}