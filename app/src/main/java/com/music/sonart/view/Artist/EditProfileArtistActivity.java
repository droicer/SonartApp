package com.music.sonart.view.Artist;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.music.sonart.R;
import com.music.sonart.model.Artist.ArtistRequest;
import com.music.sonart.model.Artist.ArtistResponse;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.view.User.MenuActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileArtistActivity extends AppCompatActivity {

    private EditText etStageName, etGenre, etBio, etInstagram, etYoutube;
    private Button btnUpdate, btnDelete;
    private String idToken;
    private String artistId; // 🔹 ID del artista para actualizar/eliminar
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_artist);

        btnBack = findViewById(R.id.btnBack);
        etStageName = findViewById(R.id.etStageName);
        etGenre = findViewById(R.id.etGenre);
        etBio = findViewById(R.id.etBio);
        etInstagram = findViewById(R.id.etInstagram);
        etYoutube = findViewById(R.id.etYoutube);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileArtistActivity.this, ArtistActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // 🔹 Obtener token del usuario autenticado
        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true)
                .addOnSuccessListener(result -> {
                    idToken = result.getToken();
                    loadArtistProfile();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show()
                );

        btnUpdate.setOnClickListener(v -> updateArtist());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadArtistProfile() {
        ApiService api = ApiClient.getApiService(idToken);
        String firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        api.getArtistByFirebase(firebaseUid).enqueue(new Callback<ArtistResponse>() {
            @Override
            public void onResponse(Call<ArtistResponse> call, Response<ArtistResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArtistResponse a = response.body();

                    // ✅ No guardes el id numérico, usa siempre el UID para las futuras peticiones
                    etStageName.setText(a.getStageName());
                    etGenre.setText(a.getGenre());
                    etBio.setText(a.getBio());

                    if (a.getSocialLinks() != null) {
                        etInstagram.setText(a.getSocialLinks().get("instagram"));
                        etYoutube.setText(a.getSocialLinks().get("youtube"));
                    }

                    Log.d("ARTIST_PROFILE", "Artista cargado correctamente: " + new Gson().toJson(a));

                } else {
                    try {
                        Log.e("ARTIST_PROFILE", " Error del servidor (" + response.code() + "): "
                                + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(EditProfileArtistActivity.this,
                            "Error al cargar artista (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArtistResponse> call, Throwable t) {
                Log.e("ARTIST_PROFILE", "Error de red: " + t.getMessage(), t);
                Toast.makeText(EditProfileArtistActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void updateArtist() {
        String stageName = etStageName.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String instagram = etInstagram.getText().toString().trim();
        String youtube = etYoutube.getText().toString().trim();

        Map<String, String> socialLinks = new HashMap<>();
        if (!instagram.isEmpty()) socialLinks.put("instagram", instagram);
        if (!youtube.isEmpty()) socialLinks.put("youtube", youtube);

        String firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ArtistRequest artistRequest = new ArtistRequest(firebaseUid, stageName, genre, bio, socialLinks);

        ApiService api = ApiClient.getApiService(idToken);
        api.updateArtistByFirebase(firebaseUid, artistRequest).enqueue(new Callback<ArtistResponse>() {
            @Override
            public void onResponse(Call<ArtistResponse> call, Response<ArtistResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileArtistActivity.this, "Artista actualizado correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        Log.e("UPDATE_ARTIST", "❌ Error: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(EditProfileArtistActivity.this, "Error al actualizar artista", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArtistResponse> call, Throwable t) {
                Toast.makeText(EditProfileArtistActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar perfil de artista")
                .setMessage("¿Seguro que deseas eliminar tu perfil de artista? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> deleteArtist())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteArtist() {
        String firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ApiService api = ApiClient.getApiService(idToken);

        api.deleteArtistByFirebase(firebaseUid).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileArtistActivity.this, "🗑️ Perfil de artista eliminado", Toast.LENGTH_SHORT).show();

                    // 🔹 Ir al menú principal
                    Intent intent = new Intent(EditProfileArtistActivity.this, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish(); // Cierra la actividad actual
                } else {
                    Toast.makeText(EditProfileArtistActivity.this, "Error al eliminar artista (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditProfileArtistActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
