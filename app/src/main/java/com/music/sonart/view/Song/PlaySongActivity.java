package com.music.sonart.view.Song;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.sonart.R;
import com.music.sonart.adapter.Song.SongListAdapter;
import com.music.sonart.databinding.ActivityPlaySongBinding;
import com.music.sonart.model.Song.Song;
import com.music.sonart.model.player.PlayerManager;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.service.MusicService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlaySongActivity extends AppCompatActivity {

    private ActivityPlaySongBinding binding;

    // === VISTAS ===
    private Button btnFollowArtist;
    private ImageView ivCover, ivLike, ivPlayPause, ivForward, ivBackward;
    private TextView tvTitle, tvArtist, tvCurrentTime, tvTotalTime, tvMoreSongsTitle, tvLikeCount;
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

    // === ESTADO DE USUARIO ===
    private int currentUserId;
    private boolean isLiked = false;
    private boolean isFollowing = false;
    private int likeCount = 0;

    private static final int PERMISO_NOTIFICACION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPlaySongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // === PEDIR PERMISO NOTIFICACIÓN (Android 13+) ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISO_NOTIFICACION);
                return;
            }
        }
        initApp();
    }

    private void initApp() {

        // === OBTENER USER_ID Y TOKEN DESDE SHARED PREFERENCES ===
        SharedPreferences prefs = getSharedPreferences("SonartPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        token = prefs.getString("token", null);

        Log.d("DEBUG_USER_ID", "User ID cargado desde prefs: " + currentUserId);

        if (currentUserId == -1) {
            Toast.makeText(this, "Error: usuario no autenticado", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // === ENLAZAR VISTAS ===
        ivCover = findViewById(R.id.ivCover);
        ivLike = findViewById(R.id.ivLike);
        btnFollowArtist = findViewById(R.id.btnFollowArtist);
        ivPlayPause = findViewById(R.id.ivPlayPause);
        ivForward = findViewById(R.id.ivForward);
        ivBackward = findViewById(R.id.ivRewind);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        tvMoreSongsTitle = findViewById(R.id.tvMoreSongsLabel);
        tvLikeCount = findViewById(R.id.tvLikeCount);
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
        binding.btnBack.setOnClickListener(v -> finish());
        ivPlayPause.setOnClickListener(v -> togglePlayPause());
        ivForward.setOnClickListener(v -> nextSong());
        ivBackward.setOnClickListener(v -> previousSong());
        ivLike.setOnClickListener(v -> toggleLike());
        btnFollowArtist.setOnClickListener(v -> toggleFollow());

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

        // === DEBUG: Verifica artistId ===
        if (song.getArtistId() == null || song.getArtistId() == 0) {
            Toast.makeText(this, "Error: artistId es nulo o 0", Toast.LENGTH_LONG).show();
        }

        registerPlay();

        Intent i = new Intent(this, MusicService.class);
        i.putExtra("song", song);
        startService(i);

        requestUpdate();
        loadLikeStatusAndCount();
        loadFollowStatus();
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
    //  LIKE SYSTEM - ACTUALIZADO
    // =============================================
    private void toggleLike() {
        Map<String, Integer> body = new HashMap<>();
        body.put("user_id", currentUserId);
        body.put("song_id", currentSong.getId());

        ApiService api = ApiClient.getApiService(token);

        if (isLiked) {
            api.unlikeSong(body).enqueue(new Callback<Map<String, Object>>() {
                @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> r) {
                    if (r.isSuccessful()) {
                        isLiked = false;
                        likeCount--;
                        updateLikeUI();
                    }
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(PlaySongActivity.this, "Error al quitar like", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            api.likeSong(body).enqueue(new Callback<Map<String, Object>>() {
                @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> r) {
                    if (r.isSuccessful()) {
                        isLiked = true;
                        likeCount++;
                        updateLikeUI();
                    }
                }
                @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Toast.makeText(PlaySongActivity.this, "Error al dar like", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadLikeStatusAndCount() {
        ApiService api = ApiClient.getApiService(token);

        // 1. Contar likes totales
        api.getLikesCount(currentSong.getId()).enqueue(new Callback<Map<String, Object>>() {
            @Override public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    likeCount = ((Number) r.body().get("total_likes")).intValue();
                    updateLikeUI();
                }
            }
            @Override public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
        });

        // 2. Verificar si YA dio like
        api.checkLike(currentUserId, currentSong.getId()).enqueue(new Callback<Map<String, Boolean>>() {
            @Override public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    isLiked = Boolean.TRUE.equals(r.body().get("has_liked"));
                    updateLikeUI();
                }
            }
            @Override public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                isLiked = false;
                updateLikeUI();
            }
        });
    }

    private void updateLikeUI() {
        ivLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        tvLikeCount.setText(String.valueOf(likeCount));
    }

    // =============================================
//  FOLLOW SYSTEM - ACTUALIZADO
// =============================================
    private void toggleFollow() {
        Integer artistId = currentSong.getArtistId();

        if (artistId == null || artistId == 0) {
            Toast.makeText(this, "Artista no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> body = new HashMap<>();
        body.put("follower_id", currentUserId);
        body.put("followed_artist_id", artistId); // ✅ Correcto

        ApiService api = ApiClient.getApiService(token);

        if (isFollowing) {
            api.unfollowUser(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> r) {
                    if (r.isSuccessful()) {
                        isFollowing = false;
                        updateFollowButton();
                        Toast.makeText(PlaySongActivity.this, "Dejaste de seguir", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("FOLLOW_DEBUG", "❌ Unfollow Error: " + r.code());
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FOLLOW_DEBUG", "💥 Error Unfollow", t);
                }
            });
        } else {
            api.followUser(body).enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> r) {
                    if (r.isSuccessful()) {
                        isFollowing = true;
                        updateFollowButton();
                        Toast.makeText(PlaySongActivity.this, "Ahora sigues al artista", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("FOLLOW_DEBUG", "❌ Follow Error: " + r.code());
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e("FOLLOW_DEBUG", "💥 Error Follow", t);
                }
            });
        }
    }

    private void loadFollowStatus() {
        if (currentSong.getArtistId() == null) return;

        ApiService api = ApiClient.getApiService(token);

        api.checkFollow(currentUserId, currentSong.getArtistId()).enqueue(new Callback<Map<String, Boolean>>() {
            @Override
            public void onResponse(Call<Map<String, Boolean>> call, Response<Map<String, Boolean>> r) {
                if (r.isSuccessful() && r.body() != null) {
                    isFollowing = Boolean.TRUE.equals(r.body().get("is_following"));
                    updateFollowButton();
                }
            }
            @Override
            public void onFailure(Call<Map<String, Boolean>> call, Throwable t) {
                Log.e("FOLLOW_DEBUG", "💥 Error checkFollow", t);
            }
        });
    }

    private void updateFollowButton() {
        btnFollowArtist.setText(isFollowing ? "Siguiendo" : "Seguir");
        btnFollowArtist.setBackgroundResource(isFollowing ?
                R.drawable.btn_following : R.drawable.btn_follow);
    }


    // =============================================
//  PLAY REGISTRATION (con respuesta visible)
// =============================================
    private void registerPlay() {
        Map<String, Integer> body = new HashMap<>();
        body.put("user_id", currentUserId);
        body.put("song_id", currentSong.getId());

        ApiClient.getApiService(token).playSong(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("PLAY_API", "✅ Éxito: " + response.body().toString());
                } else {
                    try {
                        if (response.errorBody() != null) {
                            Log.e("PLAY_API", "❌ Error: " + response.errorBody().string());
                        } else {
                            Log.e("PLAY_API", "❌ Error desconocido. Código: " + response.code());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e("PLAY_API", "❌ Fallo de conexión: " + t.getMessage());
            }
        });
    }


    // =============================================
    //  PERMISO + DESTRUCCIÓN
    // =============================================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISO_NOTIFICACION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initApp();
            } else {
                Toast.makeText(this, "Acepta el permiso para ver la notificación", Toast.LENGTH_LONG).show();
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