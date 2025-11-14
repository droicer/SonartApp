package com.music.sonart.view.Song;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.adapter.Song.SongManageAdapter;
import com.music.sonart.databinding.ActivityMySongBinding;
import com.music.sonart.model.Song.Song;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.view.Artist.ArtistActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MySongActivity extends AppCompatActivity {

    private ActivityMySongBinding binding;
    private SongManageAdapter songManageAdapter;
    private List<Song> songList;
    private String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMySongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        // Botón volver (flecha de retroceso)
        binding.btnBack.setOnClickListener(v -> finish());

        // Inicializar RecyclerView
        songList = new ArrayList<>();
        songManageAdapter = new SongManageAdapter(songList, new SongManageAdapter.OnSongManageListener() {
            @Override
            public void onEdit(Song song) {
                // Navegar a pantalla de edición
                Intent intent = new Intent(MySongActivity.this, EditSongActivity.class);
                intent.putExtra("song_id", song.getId());
                intent.putExtra("title", song.getTitle());
                intent.putExtra("genre", song.getGenre());
                intent.putExtra("coverUrl", song.getCoverUrl());
                startActivity(intent);
            }

            @Override
            public void onDelete(Song song) {
                // Confirmar eliminación
                new androidx.appcompat.app.AlertDialog.Builder(MySongActivity.this)
                        .setTitle("Eliminar canción")
                        .setMessage("¿Deseas eliminar \"" + song.getTitle() + "\"?")
                        .setPositiveButton("Eliminar", (dialog, which) -> deleteSong(song.getId()))
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.setAdapter(songManageAdapter);

        // Botón volver a ArtistActivity
        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(MySongActivity.this, ArtistActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // FAB para crear nueva canción
        binding.fabAddSong.setOnClickListener(v -> {
            Intent intent = new Intent(MySongActivity.this, CreateSongActivity.class);
            startActivity(intent);
        });

        // Obtener token de Firebase y cargar canciones
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener(result -> {
                idToken = result.getToken();
                fetchUserSongs(user.getUid());
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show();
                Log.e("MySongActivity", "Token error: " + e.getMessage());
            });
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Obtiene las canciones del usuario autenticado
     */
    private void fetchUserSongs(String firebaseUid) {
        ApiService api = ApiClient.getApiService(idToken);
        api.getSongsByFirebase(firebaseUid).enqueue(new Callback<List<Song>>() {
            @Override
            public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    songList = response.body();
                    if (songList.isEmpty()) {
                        Toast.makeText(MySongActivity.this, "No tienes canciones registradas", Toast.LENGTH_SHORT).show();
                    }
                    songManageAdapter.updateSongs(songList);
                } else {
                    try {
                        String errorBody = response.errorBody() != null
                                ? response.errorBody().string()
                                : "Error desconocido";
                        Toast.makeText(MySongActivity.this, "Error al cargar canciones: " + errorBody, Toast.LENGTH_LONG).show();
                        Log.e("MySongActivity", "API error: " + errorBody);
                    } catch (Exception e) {
                        Log.e("MySongActivity", "Error parsing response: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Song>> call, Throwable t) {
                Toast.makeText(MySongActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MySongActivity", "Network failure: " + t.getMessage());
            }
        });
    }

    /**
     * Elimina una canción del servidor
     */
    private void deleteSong(int songId) {
        if (idToken == null) return;

        ApiService api = ApiClient.getApiService(idToken);
        api.deleteSong(songId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MySongActivity.this, "Canción eliminada correctamente", Toast.LENGTH_SHORT).show();
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) fetchUserSongs(user.getUid());
                } else {
                    Toast.makeText(MySongActivity.this, "Error al eliminar canción", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(MySongActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("MySongActivity", "Error eliminando canción: " + t.getMessage());
            }
        });
    }
}
