package com.example.atividade3;
import android.Manifest;
import android.content.Intent;
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
import com.google.android.gms.maps.model.CameraPosition;
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
    private static final int REQUEST_CONFIG   = 2;
    private GoogleMap mMap;
    private Marker userMarker;
    private com.google.android.gms.maps.model.Circle accuracyCircle;
    private Polyline polyline;
    private final List<LatLng> pontos = new ArrayList<>();
    private int tipoMapa  = GoogleMap.MAP_TYPE_NORMAL;
    private boolean courseUp = false;
    private FusedLocationProviderClient fusedClient;
    private LocationCallback locationCallback;
    private Banco db;
    private long trilhaId = -1;
    private Location lastLocation;
    private double velocidadeMaxima = 0;
    private double distanciaTotal   = 0;
    private long tempoInicio = 0;
    private boolean rastreando = false;
    private final Handler cronometroHandler = new Handler();
    private Runnable cronometroRunnable;
    private TextView tvVelocidade, tvVelMax, tvDistancia, tvCronometro, tvStatus;
    private Button btnIniciar, btnParar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_trilha);
        db = new Banco(this);
        tvVelocidade = findViewById(R.id.tv_velocidade);
        tvVelMax     = findViewById(R.id.tv_vel_max);
        tvDistancia  = findViewById(R.id.tv_distancia);
        tvCronometro = findViewById(R.id.tv_cronometro);
        tvStatus     = findViewById(R.id.tv_status);
        btnIniciar   = findViewById(R.id.btn_iniciar);
        btnParar     = findViewById(R.id.btn_parar);
        btnIniciar.setOnClickListener(v -> iniciarTrilha());
        btnParar.setOnClickListener(v -> pararTrilha());
        findViewById(R.id.btn_config).setOnClickListener(v ->
                startActivityForResult(new Intent(this, Config.class), REQUEST_CONFIG));
        findViewById(R.id.btn_voltar).setOnClickListener(v -> {
            if (rastreando) {
                Toast.makeText(this, "Finalize a trilha antes de sair", Toast.LENGTH_SHORT).show();
            } else {
                finish();
            }
        });
        fusedClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONFIG && resultCode == RESULT_OK && data != null) {
            tipoMapa = data.getIntExtra("tipoMapa", GoogleMap.MAP_TYPE_NORMAL);
            courseUp = data.getBooleanExtra("courseUp", false);
            aplicarConfiguracoes();
        }
    }
    private void aplicarConfiguracoes() {
        if (mMap == null) return;
        mMap.setMapType(tipoMapa);
    }
    private void iniciarTrilha() {
        if (rastreando) return;
        String dataInicio = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());
        trilhaId = db.inserirTrilha("Trilha " + dataInicio, dataInicio, "", 0, 0, 0, "");
        velocidadeMaxima = 0;
        distanciaTotal   = 0;
        lastLocation  = null;
        pontos.clear();

        if (polyline  != null) { polyline.remove(); polyline = null; }
        if (userMarker != null) { userMarker.remove(); userMarker = null; }
        if (accuracyCircle != null) { accuracyCircle.remove(); accuracyCircle = null; }
        rastreando  = true;
        tempoInicio = SystemClock.elapsedRealtime();
        tvStatus.setText("● GRAVANDO");
        tvStatus.setTextColor(Color.parseColor("#FF4444"));

        btnIniciar.setEnabled(false);
        btnParar.setEnabled(true);
        iniciarCronometro();
        solicitarGPS();
    }
    private void pararTrilha() {
        if (!rastreando) return;
        rastreando = false;
        pararCronometro();
        pararGPS();
        long ms = SystemClock.elapsedRealtime() - tempoInicio;

        String dataFim  = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date());
        double velMedia = ms > 0 ? (distanciaTotal / 1000.0) / (ms / 3600000.0) : 0;
        String duracao  = formatarTempo(ms);

        db.finalizarTrilha(trilhaId, dataFim, velMedia, velocidadeMaxima,
                distanciaTotal / 1000.0, duracao);
        tvStatus.setText("● PARADO");
        tvStatus.setTextColor(Color.parseColor("#888888"));

        btnIniciar.setEnabled(true);
        btnParar.setEnabled(false);
        Toast.makeText(this, "Trilha salva!", Toast.LENGTH_SHORT).show();
    }
    private void solicitarGPS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }
        LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateDistanceMeters(1).build();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult r) {
                if (r.getLastLocation() != null) onNovaLocalizacao(r.getLastLocation());
            }
        };
        fusedClient.requestLocationUpdates(req, locationCallback, getMainLooper());
    }
    private void pararGPS() {
        if (locationCallback != null) {
            fusedClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }
    private void onNovaLocalizacao(Location loc) {
        double vel = loc.getSpeed() * 3.6;
        if (vel > velocidadeMaxima) velocidadeMaxima = vel;
        if (lastLocation != null) distanciaTotal += lastLocation.distanceTo(loc);
        lastLocation = loc;
        if (rastreando && trilhaId != -1)
            db.inserirPonto(trilhaId, loc.getLatitude(), loc.getLongitude());
        tvVelocidade.setText(String.format(Locale.getDefault(), "%.1f km/h", vel));
        tvVelMax.setText(String.format(Locale.getDefault(), "%.1f km/h", velocidadeMaxima));
        tvDistancia.setText(distanciaTotal < 1000
                ? String.format(Locale.getDefault(), "%.0f m", distanciaTotal)
                : String.format(Locale.getDefault(), "%.2f km", distanciaTotal / 1000.0));
        atualizarMapa(loc);
    }
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
        float bearing = courseUp ? loc.getBearing() : 0f;
        CameraPosition cam = new CameraPosition.Builder()
                .target(pos)
                .zoom(18)
                .bearing(bearing)
                .tilt(courseUp ? 45 : 0)
                .build();
        if (userMarker != null && pontos.size() == 1) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cam));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cam));
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(tipoMapa);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        fusedClient.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null && mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(loc.getLatitude(), loc.getLongitude()), 17));
            }
        });
    }
    private BitmapDescriptor bitmapFromVector(int resId, int w, int h) {
        Drawable d = ContextCompat.getDrawable(this, resId);
        if (d == null) return BitmapDescriptorFactory.defaultMarker();
        d.setBounds(0, 0, w, h);
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        d.draw(new Canvas(bmp));
        return BitmapDescriptorFactory.fromBitmap(bmp);
    }
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
    @Override
    public void onRequestPermissionsResult(int req, String[] perms, int[] results) {
        super.onRequestPermissionsResult(req, perms, results);
        if (req == REQUEST_LOCATION && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            if (rastreando) solicitarGPS();
        } else {
            Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        pararGPS();
        pararCronometro();
    }
}