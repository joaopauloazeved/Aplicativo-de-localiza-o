package com.example.atividade3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistrarTrilhaActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION = 1;

    // Map
    private GoogleMap mMap;
    private Marker userMarker;
    private com.google.android.gms.maps.model.Circle accuracyCircle;
    private Polyline polyline;
    private final List<LatLng> pontos = new ArrayList<>();

    // Location
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;
    private Location lastLocation;

    // Posição de partida (GPS real ou última conhecida)
    private double latInicial = 0;
    private double lngInicial = 0;
    private boolean temPosicaoInicial = false;

    // Database
    private DatabaseHelper db;
    private long trilhaId = -1;
    private String dataInicio;

    // Stats
    private double velocidadeAtual  = 0;
    private double velocidadeMaxima = 0;
    private double distanciaTotal   = 0;

    // Cronômetro
    private long tempoInicio = 0;
    private boolean rastreando = false;
    private final Handler cronometroHandler = new Handler();
    private Runnable cronometroRunnable;

    // Simulação
    private boolean modoSimulacao = false;
    private final Handler simulacaoHandler = new Handler();
    private Runnable simulacaoRunnable;
    private int simIndex = 0;
    private LatLng[] rotaSimulada;

    // UI
    private TextView tvVelocidade, tvVelMax, tvDistancia, tvCronometro, tvStatus;
    private Button btnIniciar, btnParar, btnSimular;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_trilha);

        db = new DatabaseHelper(this);

        tvVelocidade = findViewById(R.id.tv_velocidade);
        tvVelMax     = findViewById(R.id.tv_vel_max);
        tvDistancia  = findViewById(R.id.tv_distancia);
        tvCronometro = findViewById(R.id.tv_cronometro);
        tvStatus     = findViewById(R.id.tv_status);
        btnIniciar   = findViewById(R.id.btn_iniciar);
        btnParar     = findViewById(R.id.btn_parar);
        btnSimular   = findViewById(R.id.btn_simular);

        findViewById(R.id.btn_voltar).setOnClickListener(v -> {
            if (rastreando) {
                Toast.makeText(this, "Finalize a trilha antes de sair", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        });

        btnIniciar.setOnClickListener(v -> iniciarTrilha());
        btnParar.setOnClickListener(v -> pararTrilha());

        btnSimular.setOnClickListener(v -> {
            modoSimulacao = !modoSimulacao;
            if (modoSimulacao) {
                btnSimular.setText("🧪 SIMULAÇÃO: ON");
                btnSimular.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#2E7D32")));
                Toast.makeText(this, "Modo simulação ativado. Clique em INICIAR.", Toast.LENGTH_SHORT).show();
            } else {
                btnSimular.setText("🧪 MODO SIMULAÇÃO");
                btnSimular.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor("#E65100")));
            }
        });

        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Tenta obter posição atual logo ao abrir, para usar como base da simulação
        obterPosicaoInicial();
    }

    // ──────────────── POSIÇÃO INICIAL ────────────────

    private void obterPosicaoInicial() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                latInicial = loc.getLatitude();
                lngInicial = loc.getLongitude();
                temPosicaoInicial = true;
            }
        });
    }

    // ──────────────── GERAR ROTA SIMULADA ────────────────
    //
    // Gera 30 pontos a partir da posição de origem, fazendo um percurso
    // em forma de "L": vai para leste, depois para norte, depois volta.
    // Cada passo ~ 0,0001 grau ≈ 11 metros → total ≈ 330 m.

    private LatLng[] gerarRotaSimulada(double latBase, double lngBase) {
        List<LatLng> rota = new ArrayList<>();

        // Segmento 1: 10 passos para leste
        for (int i = 0; i <= 10; i++) {
            rota.add(new LatLng(latBase, lngBase + i * 0.0001));
        }
        // Segmento 2: 10 passos para norte
        double lngLeste = lngBase + 10 * 0.0001;
        for (int i = 1; i <= 10; i++) {
            rota.add(new LatLng(latBase - i * 0.0001, lngLeste));
        }
        // Segmento 3: volta para o ponto inicial
        double latNorte = latBase - 10 * 0.0001;
        for (int i = 1; i <= 10; i++) {
            rota.add(new LatLng(latNorte, lngLeste - i * 0.0001));
        }
        for (int i = 1; i <= 10; i++) {
            rota.add(new LatLng(latNorte + i * 0.0001, lngBase));
        }

        return rota.toArray(new LatLng[0]);
    }

    // ──────────────── INICIAR / PARAR ────────────────

    private void iniciarTrilha() {
        if (rastreando) return;

        dataInicio = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());
        trilhaId = db.inserirTrilha("Trilha " + dataInicio, dataInicio, "", 0, 0, 0, "");

        velocidadeMaxima = 0;
        distanciaTotal   = 0;
        lastLocation     = null;
        pontos.clear();
        simIndex         = 0;

        if (polyline     != null) { polyline.remove();       polyline       = null; }
        if (userMarker   != null) { userMarker.remove();     userMarker     = null; }
        if (accuracyCircle != null) { accuracyCircle.remove(); accuracyCircle = null; }

        rastreando  = true;
        tempoInicio = SystemClock.elapsedRealtime();

        tvStatus.setText("● GRAVANDO");
        tvStatus.setTextColor(Color.parseColor("#FF4444"));
        btnIniciar.setEnabled(false);
        btnParar.setEnabled(true);
        btnSimular.setEnabled(false);

        iniciarCronometro();

        if (modoSimulacao) {
            // Usa posição real obtida ao abrir a tela; senão usa ponto padrão (UCSAL)
            double latBase = temPosicaoInicial ? latInicial : -12.94825;
            double lngBase = temPosicaoInicial ? lngInicial : -38.41334;
            rotaSimulada   = gerarRotaSimulada(latBase, lngBase);
            iniciarSimulacao();
        } else {
            solicitarAtualizacoes();
        }
    }

    private void pararTrilha() {
        if (!rastreando) return;
        rastreando = false;

        pararCronometro();
        pararSimulacao();
        pararAtualizacoes();

        long duracaoMs  = SystemClock.elapsedRealtime() - tempoInicio;
        String dataFim  = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());
        double velMedia = duracaoMs > 0
                ? (distanciaTotal / 1000.0) / (duracaoMs / 3600000.0) : 0;
        String duracao  = formatarTempo(duracaoMs);

        db.finalizarTrilha(trilhaId, dataFim, velMedia, velocidadeMaxima,
                distanciaTotal / 1000.0, duracao);

        tvStatus.setText("● PARADO");
        tvStatus.setTextColor(Color.parseColor("#888888"));
        btnIniciar.setEnabled(true);
        btnParar.setEnabled(false);
        btnSimular.setEnabled(true);

        Toast.makeText(this, "Trilha salva com sucesso!", Toast.LENGTH_SHORT).show();
    }

    // ──────────────── SIMULAÇÃO ────────────────

    private void iniciarSimulacao() {
        simulacaoRunnable = new Runnable() {
            @Override
            public void run() {
                if (!rastreando) return;

                LatLng coord = rotaSimulada[simIndex];

                Location loc = new Location("simulado");
                loc.setLatitude(coord.latitude);
                loc.setLongitude(coord.longitude);
                loc.setAccuracy(8f);

                // Velocidade calculada a partir da distância entre pontos consecutivos
                if (simIndex > 0 && lastLocation != null) {
                    float dist = lastLocation.distanceTo(loc);
                    loc.setSpeed(dist / 2.0f); // intervalo 2s → m/s
                } else {
                    loc.setSpeed(3.5f); // ~12,6 km/h no primeiro ponto
                }

                onNovaLocalizacao(loc);
                simIndex++;

                if (simIndex >= rotaSimulada.length) {
                    pararTrilha();
                    return;
                }

                simulacaoHandler.postDelayed(this, 2000);
            }
        };
        simulacaoHandler.post(simulacaoRunnable);
    }

    private void pararSimulacao() {
        if (simulacaoRunnable != null) {
            simulacaoHandler.removeCallbacks(simulacaoRunnable);
            simulacaoRunnable = null;
        }
    }

    // ──────────────── GPS REAL ────────────────

    private void solicitarAtualizacoes() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateDistanceMeters(2)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                Location loc = result.getLastLocation();
                if (loc != null) onNovaLocalizacao(loc);
            }
        };

        fusedClient.requestLocationUpdates(req, locationCallback, getMainLooper());
    }

    private void pararAtualizacoes() {
        if (locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    // ──────────────── PROCESSA LOCALIZAÇÃO ────────────────

    private void onNovaLocalizacao(Location loc) {
        velocidadeAtual = loc.getSpeed() * 3.6;
        if (velocidadeAtual > velocidadeMaxima) velocidadeMaxima = velocidadeAtual;

        if (lastLocation != null) distanciaTotal += lastLocation.distanceTo(loc);
        lastLocation = loc;

        if (rastreando && trilhaId != -1)
            db.inserirPonto(trilhaId, loc.getLatitude(), loc.getLongitude());

        tvVelocidade.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidadeAtual));
        tvVelMax.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidadeMaxima));
        double km = distanciaTotal / 1000.0;
        tvDistancia.setText(km < 1
                ? String.format(Locale.getDefault(), "%.0f m", distanciaTotal)
                : String.format(Locale.getDefault(), "%.2f km", km));

        atualizarMapa(loc);
    }

    // ──────────────── MAPA ────────────────

    private void atualizarMapa(Location loc) {
        if (mMap == null) return;
        LatLng pos = new LatLng(loc.getLatitude(), loc.getLongitude());
        pontos.add(pos);

        if (userMarker == null) {
            BitmapDescriptor icon = bitmapFromVector(R.drawable.ic_usuario_marker, 80, 80);
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(pos).icon(icon).anchor(0.5f, 0.5f).title("Você está aqui"));
        } else {
            userMarker.setPosition(pos);
        }

        if (accuracyCircle == null) {
            accuracyCircle = mMap.addCircle(new CircleOptions()
                    .center(pos).radius(loc.getAccuracy())
                    .strokeColor(Color.parseColor("#4499CCFF"))
                    .strokeWidth(2)
                    .fillColor(Color.parseColor("#2299CCFF")));
        } else {
            accuracyCircle.setCenter(pos);
            accuracyCircle.setRadius(loc.getAccuracy());
        }

        if (polyline == null) {
            polyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(pontos).color(Color.parseColor("#FF4444")).width(8));
        } else {
            polyline.setPoints(pontos);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
    }

    private BitmapDescriptor bitmapFromVector(int resourceId, int w, int h) {
        Drawable drawable = ContextCompat.getDrawable(this, resourceId);
        if (drawable == null) return BitmapDescriptorFactory.defaultMarker();
        drawable.setBounds(0, 0, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawable.draw(new Canvas(bmp));
        return BitmapDescriptorFactory.fromBitmap(bmp);
    }

    // ──────────────── CRONÔMETRO ────────────────

    private void iniciarCronometro() {
        cronometroRunnable = new Runnable() {
            @Override
            public void run() {
                tvCronometro.setText(formatarTempo(SystemClock.elapsedRealtime() - tempoInicio));
                cronometroHandler.postDelayed(this, 1000);
            }
        };
        cronometroHandler.post(cronometroRunnable);
    }

    private void pararCronometro() {
        if (cronometroRunnable != null)
            cronometroHandler.removeCallbacks(cronometroRunnable);
    }

    private String formatarTempo(long ms) {
        long s = ms / 1000;
        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                s / 3600, (s % 3600) / 60, s % 60);
    }

    // ──────────────── LIFECYCLE ────────────────

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == REQUEST_LOCATION && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            obterPosicaoInicial();
            if (rastreando) solicitarAtualizacoes();
        } else {
            Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararAtualizacoes();
        pararCronometro();
        pararSimulacao();
    }
}