// PlaySongActivity.java → COPIA TODO (YA TIENE TODO)
package com.music.sonart.view.Song;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.sonart.R;
import com.music.sonart.adapter.SongListAdapter;
import com.music.sonart.model.Song;
import com.music.sonart.model.player.PlayerManager;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.service.MusicService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaySongActivity extends AppCompatActivity {

    // === VISTAS ===
    private ImageView ivCover, ivLike, ivPlayPause, ivForward, ivBackward;
    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime, tvMoreSongsTitle;
    private SeekBar seekBar;
    private RecyclerView rvMoreSongs;

    // === DATOS ===
    private SongListAdapter moreSongsAdapter;
    private Handler handler = new Handler();
    private Song currentSong;
    private String token;
    private BroadcastReceiver stateReceiver;
    private BroadcastReceiver nextSongReceiver;
    private List<Song> artistSongs = new ArrayList<>();
    private List<Song> allSongs = new ArrayList<>();

    private static final int PERMISO_NOTIFICACION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);

        // === PEDIR PERMISO NOTIFICACIÓN (Android 13+) ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISO_NOTIFICACION);
                return; // Espera al permiso
            }
        }

        initApp(); // ← Solo se ejecuta si tiene permiso
    }

    private void initApp() {
        // === ENLAZAR VISTAS ===
        ivCover = findViewById(R.id.ivCover);
        ivLike = findViewById(R.id.ivLike);
        ivPlayPause = findViewById(R.id.ivPlayPause);
        ivForward = findViewById(R.id.ivForward);
        ivBackward = findViewById(R.id.ivRewind);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvMoreSongsTitle = findViewById(R.id.tvMoreSongsLabel);
        seekBar = findViewById(R.id.seekBar);
        rvMoreSongs = findViewById(R.id.rvMoreSongs);

        // === RECIBIR DATOS ===
        currentSong = (Song) getIntent().getSerializableExtra("song");
        token = getIntent().getStringExtra("token");

        // === ADAPTER ===
        moreSongsAdapter = new SongListAdapter(new ArrayList<>(), song -> stopCurrentAndPlayNew(song));
        rvMoreSongs.setLayoutManager(new LinearLayoutManager(this));
        rvMoreSongs.setAdapter(moreSongsAdapter);

        // === BOTONES ===
        ivPlayPause.setOnClickListener(v -> togglePlayPause());
        ivForward.setOnClickListener(v -> nextSong());
        ivBackward.setOnClickListener(v -> previousSong());
        ivLike.setOnClickListener(v -> Toast.makeText(this, "❤️ Guardada", Toast.LENGTH_SHORT).show());

        // === SEEK BAR ===
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean u) {
                if (u) seekTo(p);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // === CARGAR DATOS ===
        if (currentSong != null) {
            setupSong(currentSong);
            loadArtistSongs();
            loadAllSongs();
        }

        // === ESCUCHAR FIN DE CANCIÓN ===
        nextSongReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                handler.postDelayed(PlaySongActivity.this::nextSong, 200);
            }
        };
        registerReceiver(nextSongReceiver, new IntentFilter(MusicService.ACTION_NEXT_SONG));
    }

    // =============================================
    //  CAMBIAR CANCIÓN
    // =============================================
    private void stopCurrentAndPlayNew(Song newSong) {
        Intent pause = new Intent(this, MusicService.class);
        pause.setAction(MusicService.ACTION_PAUSE);
        startService(pause);

        handler.postDelayed(() -> {
            currentSong = newSong;
            setupSong(newSong);
            loadArtistSongs();
        }, 100);
    }

    private void setupSong(Song song) {
        currentSong = song;
        PlayerManager.getInstance().setCurrentSong(song);

        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtistName());
        tvMoreSongsTitle.setText("Más de " + song.getArtistName());
        Glide.with(this).load(song.getCoverUrl()).into(ivCover);

        Intent i = new Intent(this, MusicService.class);
        i.putExtra("song", song);
        startService(i);

        requestUpdate();
    }

    // =============================================
    //  SIGUIENTE / ANTERIOR
    // =============================================
    private void nextSong() {
        Song next = getNextSongFromArtist();
        if (next == null) next = getRandomSong();
        if (next != null) stopCurrentAndPlayNew(next);
    }

    private void previousSong() {
        Song prev = getPreviousSongFromArtist();
        if (prev != null) stopCurrentAndPlayNew(prev);
    }

    private Song getNextSongFromArtist() {
        int i = artistSongs.indexOf(currentSong);
        return (i < artistSongs.size() - 1) ? artistSongs.get(i + 1) : null;
    }

    private Song getPreviousSongFromArtist() {
        int i = artistSongs.indexOf(currentSong);
        return (i > 0) ? artistSongs.get(i - 1) : null;
    }

    private Song getRandomSong() {
        if (allSongs.isEmpty()) return null;
        return allSongs.get(new Random().nextInt(allSongs.size()));
    }

    // =============================================
    //  CONTROLES
    // =============================================
    private void togglePlayPause() {
        Intent i = new Intent(this, MusicService.class);
        i.setAction(PlayerManager.getInstance().isPlaying() ? MusicService.ACTION_PAUSE : MusicService.ACTION_PLAY);
        startService(i);
    }

    private void seekTo(int progress) {
        Intent i = new Intent(this, MusicService.class);
        i.setAction(MusicService.ACTION_SEEK);
        i.putExtra("progress", progress);
        startService(i);
    }

    // =============================================
    //  UI
    // =============================================
    private void updateUIFromState(boolean playing, int pos, int dur) {
        ivPlayPause.setImageResource(playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        tvCurrentTime.setText(format(pos));
        tvTotalTime.setText(format(dur));
        if (dur > 0) seekBar.setProgress(pos * 100 / dur);
    }

    private String format(int ms) {
        int s = ms / 1000;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    // =============================================
    //  BROADCAST
    // =============================================
    @Override
    protected void onResume() {
        super.onResume();
        stateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                boolean p = i.getBooleanExtra("isPlaying", false);
                int pos = i.getIntExtra("currentPosition", 0);
                int dur = i.getIntExtra("duration", 0);
                updateUIFromState(p, pos, dur);
            }
        };
        registerReceiver(stateReceiver, new IntentFilter(MusicService.ACTION_STATE_CHANGED));
        requestUpdate();
        handler.postDelayed(updateRunnable, 800);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (stateReceiver != null) unregisterReceiver(stateReceiver);
        handler.removeCallbacks(updateRunnable);
    }

    private final Runnable updateRunnable = this::requestUpdate;

    private void requestUpdate() {
        startService(new Intent(this, MusicService.class).setAction(MusicService.ACTION_REQUEST_UPDATE));
        handler.postDelayed(updateRunnable, 800);
    }

    // =============================================
    //  CARGAR CANCIONES
    // =============================================
    private void loadArtistSongs() {
        if (currentSong == null || currentSong.getArtistId() == null) return;
        ApiService api = ApiClient.getApiService(token);
        api.getSongsByArtist(currentSong.getArtistId()).enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    artistSongs = r.body();
                    List<Song> list = new ArrayList<>(artistSongs);
                    list.removeIf(s -> s.getId().equals(currentSong.getId()));
                    moreSongsAdapter.updateSongs(list);
                }
            }
            @Override public void onFailure(Call<List<Song>> call, Throwable t) {}
        });
    }

    private void loadAllSongs() {
        ApiService api = ApiClient.getApiService(token);
        api.getSongs().enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> r) {
                if (r.isSuccessful() && r.body() != null) allSongs = r.body();
            }
            @Override public void onFailure(Call<List<Song>> call, Throwable t) {}
        });
    }

    // =============================================
    //  PERMISO + BOTÓN X EN NOTIFICACIÓN
    // =============================================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_NOTIFICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initApp(); // ← Ahora sí inicia
            } else {
                Toast.makeText(this, "⚠️ Acepta el permiso para ver la notificación", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(nextSongReceiver);
        } catch (Exception ignored) {}
        handler.removeCallbacksAndMessages(null);
    }
}