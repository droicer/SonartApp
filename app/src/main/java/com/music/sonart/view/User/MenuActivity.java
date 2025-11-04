package com.music.sonart.view.User;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.music.sonart.adapter.SongAdapter;
import com.music.sonart.fragment.MiniPlayerFragment;
import com.music.sonart.model.Song;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.music.sonart.R;
import com.music.sonart.view.Login.LoginActivity;
import com.music.sonart.view.Song.PlaySongActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private RecyclerView recyclerViewSongs;
    private FloatingActionButton fabAddSong;
    private SongAdapter songAdapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Firebase
        mAuth = FirebaseAuth.getInstance();

        // Toolbar + Drawer
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // RecyclerView
        recyclerViewSongs = findViewById(R.id.recyclerViewSongs);
        recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(new ArrayList<>(), song -> {
            Intent intent = new Intent(MenuActivity.this, PlaySongActivity.class);
            intent.putExtra("song", song);
            // Pasa el token también
            FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                    .addOnSuccessListener(result -> {
                        intent.putExtra("token", result.getToken());
                        startActivity(intent);
                    });
        });
        recyclerViewSongs.setAdapter(songAdapter);

        // Cargar canciones
        loadSongs();

        // AÑADIR MINI PLAYER AQUÍ
        if (findViewById(R.id.mini_player_container) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mini_player_container, new MiniPlayerFragment())
                    .commit();
        }
    }

    private void loadSongs() {
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String idToken = task.getResult().getToken();
                Log.d(TAG, "Token enviado: " + idToken);
                ApiService apiService = ApiClient.getApiService(idToken);
                Call<List<Song>> call = apiService.getSongs();
                call.enqueue(new Callback<List<Song>>() {
                    @Override
                    public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                        if (response.isSuccessful()) {
                            List<Song> songs = response.body();
                            songAdapter.updateSongs(songs);
                        } else {
                            Log.e(TAG, "Error al cargar canciones: " + response.code() + ", Mensaje: " + response.message());
                            try {
                                Log.e(TAG, "Cuerpo del error: " + response.errorBody().string());
                            } catch (IOException e) {
                                Log.e(TAG, "Error al leer errorBody", e);
                            }
                            Toast.makeText(MenuActivity.this, "Error al cargar canciones", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Song>> call, Throwable t) {
                        Log.e(TAG, "Error de red al cargar canciones", t);
                        Toast.makeText(MenuActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e(TAG, "Error al obtener token", task.getException());
                Toast.makeText(MenuActivity.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Ya estás en la pantalla principal
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_artists) {
            // Navegar a pantalla de artistas
            Toast.makeText(this, "Artistas (implementar)", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            // Navegar a pantalla de ajustes
            Toast.makeText(this, "Ajustes (implementar)", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_logout) {
            // Cerrar sesión
            mAuth.signOut();
            startActivity(new Intent(MenuActivity.this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}