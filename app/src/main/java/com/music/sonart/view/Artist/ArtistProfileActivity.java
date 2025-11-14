package com.music.sonart.view.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.music.sonart.R;
import com.music.sonart.adapter.SocialLinkAdapter;
import com.music.sonart.adapter.Song.SongListAdapter;
import com.music.sonart.databinding.ActivityArtistProfileBinding;
import com.music.sonart.fragment.MiniPlayerFragment;
import com.music.sonart.model.Search.SearchResponse;
import com.music.sonart.model.Search.SocialLink;
import com.music.sonart.model.Song.Song;
import com.music.sonart.network.ApiClient;
import com.music.sonart.view.Song.PlaySongActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArtistProfileActivity extends AppCompatActivity {

    private ActivityArtistProfileBinding binding;
    private SongListAdapter songAdapter;
    private SocialLinkAdapter socialAdapter;
    private String idToken;
    private SearchResponse.Artist artist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtistProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        artist = getIntent().getParcelableExtra("artist");
        if (artist == null) {
            Toast.makeText(this, "Artista no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupRecyclerViews();
        getFirebaseToken();  // 🔥 Obtener token ANTES de cargar canciones

        // Mini Player
        if (findViewById(R.id.mini_player_container) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mini_player_container, new MiniPlayerFragment())
                    .commit();
        }
    }

    /**
     * 🔥 Corrección:
     * Cargar canciones recién cuando tengamos el token.
     */
    private void getFirebaseToken() {
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) return;

        user.getIdToken(true).addOnSuccessListener(result -> {
            idToken = result.getToken();
            loadArtistSongs(); // 🔥 AHORA sí cargamos las canciones
        });
    }

    private void setupUI() {

        // Foto del artista
        Glide.with(this)
                .load(artist.profileImage)
                .placeholder(R.drawable.placeholder_image)
                .circleCrop()
                .into(binding.ivArtistPhoto);

        // Nombre
        binding.tvArtistName.setText(artist.stageName);

        // Verificado
        binding.ivVerified.setVisibility(artist.verified ? View.VISIBLE : View.GONE);

        // Género
        binding.tvGenre.setText(artist.genre != null ? artist.genre : "Sin género");

        // Biografía
        binding.tvBio.setText(
                artist.bio != null && !artist.bio.isEmpty() ? artist.bio : "Sin biografía"
        );

        binding.btnBack.setOnClickListener(v -> finish());

        setupSocialLinks();
    }

    private void setupSocialLinks() {
        socialAdapter = new SocialLinkAdapter();
        binding.rvSocialLinks.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSocialLinks.setAdapter(socialAdapter);

        if (artist.socialLinks == null || artist.socialLinks.isEmpty()) {
            binding.rvSocialLinks.setVisibility(View.GONE);
            return;
        }

        List<SocialLink> linkList = new ArrayList<>();
        for (Map.Entry<String, String> entry : artist.socialLinks.entrySet()) {
            String platform = capitalize(entry.getKey());
            String url = entry.getValue();
            int icon = getIconForPlatform(entry.getKey());

            linkList.add(new SocialLink(platform, url, icon));
        }

        socialAdapter.updateLinks(linkList);
        binding.rvSocialLinks.setVisibility(View.VISIBLE);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private int getIconForPlatform(String platform) {
        if (platform == null) return R.drawable.ic_link;
        String key = platform.toLowerCase().trim();

        switch (key) {
            case "youtube": return R.drawable.ic_youtube;
            case "instagram": return R.drawable.ic_instagram;
            case "tiktok": return R.drawable.ic_tiktok;
            case "twitter":
            case "x": return R.drawable.ic_twitter;
            case "facebook": return R.drawable.ic_facebook;
            case "spotify": return R.drawable.ic_spotify;
            case "soundcloud": return R.drawable.ic_soundcloud;
            default: return R.drawable.ic_link;
        }
    }

    private void setupRecyclerViews() {
        songAdapter = new SongListAdapter(new ArrayList<>(), song -> {
            Intent intent = new Intent(this, PlaySongActivity.class);
            intent.putExtra("song", song);
            startActivity(intent);
        });

        binding.rvSongs.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSongs.setAdapter(songAdapter);
    }

    /**
     * 🔥 Corrección crítica:
     * - No se llamaba con token disponible
     * - Lista vacía → no se actualizaba
     */
    private void loadArtistSongs() {

        if (idToken == null) return;
        if (artist.id <= 0) return;

        ApiClient.getApiService(idToken)
                .getSongsByArtist(artist.id)
                .enqueue(new Callback<List<Song>>() {
                    @Override
                    public void onResponse(Call<List<Song>> call, Response<List<Song>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            binding.tvSongCount.setText("0 canciones");
                            return;
                        }

                        List<Song> songs = response.body();

                        // 🔥 Asegurar que cada canción tenga su artistId
                        for (Song s : songs) {
                            s.setArtistId(artist.id);
                        }

                        songAdapter.updateSongs(songs);

                        binding.tvSongCount.setText(
                                songs.size() + " canción" + (songs.size() == 1 ? "" : "es")
                        );
                    }

                    @Override
                    public void onFailure(Call<List<Song>> call, Throwable t) {
                        binding.tvSongCount.setText("Error al cargar");
                    }
                });
    }
}
