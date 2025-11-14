package com.music.sonart.view.User;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.R;
import com.music.sonart.adapter.Song.SongAdapter;
import com.music.sonart.fragment.MiniPlayerFragment;
import com.music.sonart.model.Artist.Artist;
import com.music.sonart.model.Song.Song;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.view.Song.PlaySongActivity;
import com.music.sonart.view.Song.SearchActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MenuActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private RecyclerView recyclerRandom, recyclerLiked, recyclerFollowed;
    private SongAdapter adapterRandom, adapterLiked, adapterFollowed;

    private FirebaseAuth mAuth;
    private SharedPreferences prefs;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("SonartPrefs", Context.MODE_PRIVATE);

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

        // SwipeRefreshLayout
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadHomeContent);

        // RecyclerViews y Adapters
        recyclerRandom = findViewById(R.id.recyclerRandom);
        recyclerLiked = findViewById(R.id.recyclerLiked);
        recyclerFollowed = findViewById(R.id.recyclerFollowed);

        adapterRandom = new SongAdapter(new ArrayList<>(), this::playSong);
        adapterLiked = new SongAdapter(new ArrayList<>(), this::playSong);
        adapterFollowed = new SongAdapter(new ArrayList<>(), this::playSong);

        recyclerRandom.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerLiked.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerFollowed.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        recyclerRandom.setAdapter(adapterRandom);
        recyclerLiked.setAdapter(adapterLiked);
        recyclerFollowed.setAdapter(adapterFollowed);

        ImageView btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        });

        // Mini Player
        if (findViewById(R.id.mini_player_container) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mini_player_container, new MiniPlayerFragment())
                    .commit();
        }

        // 🔹 Guardar el ID real del usuario en SharedPreferences
        fetchAndSaveUserId();

        // 🔹 Cargar contenido
        loadHomeContent();
    }

    private void playSong(Song song) {
        Intent intent = new Intent(MenuActivity.this, PlaySongActivity.class);
        intent.putExtra("song", song);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener(result -> {
                intent.putExtra("token", result.getToken());
                startActivity(intent);
            });
        }
    }

    private void fetchAndSaveUserId() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) return;

        String email = firebaseUser.getEmail();
        firebaseUser.getIdToken(true).addOnSuccessListener(result -> {
            String token = result.getToken();
            Log.d("MiAppToken", "Token de Firebase: " + token);
            ApiService apiService = ApiClient.getApiService(token);
            Call<Map<String, Object>> call = apiService.getUserByEmail(email);

            call.enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> data = response.body();
                        if (Boolean.TRUE.equals(data.get("success"))) {
                            int userId = ((Double) data.get("user_id")).intValue();
                            prefs.edit().putInt("user_id", userId).apply();
                            Log.d(TAG, "✅ user_id guardado: " + userId);
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    Log.e(TAG, "❌ Error de red al obtener user_id", t);
                }
            });
        });
    }

    private void loadHomeContent() {
        if (mAuth.getCurrentUser() == null) {
            swipeRefresh.setRefreshing(false);
            return;
        }

        mAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(MenuActivity.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                swipeRefresh.setRefreshing(false);
                return;
            }

            String token = task.getResult().getToken();
            ApiService apiService = ApiClient.getApiService(token);

            // 🎵 Canciones aleatorias
            apiService.getRandomSongs().enqueue(new Callback<List<Song>>() {
                @Override
                public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapterRandom.updateSongs(response.body());
                    }
                    stopRefreshing();
                }

                @Override
                public void onFailure(Call<List<Song>> call, Throwable t) {
                    Log.e(TAG, "Error random songs", t);
                    stopRefreshing();
                }
            });

            // 💖 Canciones que te gustan
            int userId = getUserIdFromPrefs();
            if (userId != -1) {
                apiService.getLikedSongs(userId).enqueue(new Callback<List<Song>>() {
                    @Override
                    public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            adapterLiked.updateSongs(response.body());
                        }
                        stopRefreshing();
                    }

                    @Override
                    public void onFailure(Call<List<Song>> call, Throwable t) {
                        Log.e(TAG, "Error liked songs", t);
                        stopRefreshing();
                    }
                });

                // 🎤 Artistas seguidos → pseudo-canciones
                apiService.getFollowedArtists(userId).enqueue(new Callback<List<Artist>>() {
                    @Override
                    public void onResponse(Call<List<Artist>> call, Response<List<Artist>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Song> pseudoSongs = new ArrayList<>();
                            for (Artist artist : response.body()) {
                                Song s = new Song();
                                s.setTitle("🎤 " + artist.getStageName());
                                s.setCoverUrl(artist.getProfileImage());
                                s.setArtistName("Artista que sigues");
                                pseudoSongs.add(s);
                            }
                            adapterFollowed.updateSongs(pseudoSongs);
                        }
                        stopRefreshing();
                    }

                    @Override
                    public void onFailure(Call<List<Artist>> call, Throwable t) {
                        Log.e(TAG, "Error followed artists", t);
                        stopRefreshing();
                    }
                });
            }
        });
    }

    private void stopRefreshing() {
        if (swipeRefresh != null && swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(false);
        }
    }

    private int getUserIdFromPrefs() {
        return prefs.getInt("user_id", -1);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutUser() {
        Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show();
        mAuth.signOut();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient googleClient = GoogleSignIn.getClient(this, gso);
        googleClient.revokeAccess().addOnCompleteListener(task -> {
            prefs.edit().clear().apply();
            try {
                stopService(new Intent(MenuActivity.this, com.music.sonart.service.MusicService.class));
            } catch (Exception e) { Log.e(TAG, "Error detener MusicService", e); }

            Intent intent = new Intent(getApplicationContext(), com.music.sonart.view.Login.LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });
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
