package com.music.sonart.view.Artist;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.music.sonart.R;
import com.music.sonart.model.Artist.ArtistRequest;
import com.music.sonart.model.Artist.ArtistResponse;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.view.User.MenuActivity;
import com.music.sonart.view.User.ProfileActivity;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateArtistActivity extends AppCompatActivity {

    private EditText etStageName, etGenre, etBio, etInstagram, etYoutube;
    private Button btnCreateArtist;
    private String idToken; // 🔸 Token JWT de Firebase
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_create);

        etStageName = findViewById(R.id.etStageName);
        etGenre = findViewById(R.id.etGenre);
        etBio = findViewById(R.id.etBio);
        etInstagram = findViewById(R.id.etInstagram);
        etYoutube = findViewById(R.id.etYoutube);
        btnCreateArtist = findViewById(R.id.btnCreateArtist);
        btnBack = findViewById(R.id.btnBack);

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(CreateArtistActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });


        // 🔹 Obtener el token de Firebase antes de llamar a la API
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener(result -> {
                idToken = result.getToken(); // guardar el token
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show();
            });
        }

        btnCreateArtist.setOnClickListener(v -> createArtist());
    }

    private void createArtist() {
        if (idToken == null) {
            Toast.makeText(this, "Esperando token de autenticación...", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Obtener UID del usuario actual
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String firebaseUid = user.getUid();
        String stageName = etStageName.getText().toString().trim();
        String genre = etGenre.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String instagram = etInstagram.getText().toString().trim();
        String youtube = etYoutube.getText().toString().trim();

        if (stageName.isEmpty()) {
            etStageName.setError("El nombre artístico es obligatorio");
            return;
        }

        Map<String, String> socialLinks = new HashMap<>();
        if (!instagram.isEmpty()) socialLinks.put("instagram", instagram);
        if (!youtube.isEmpty()) socialLinks.put("youtube", youtube);

        ArtistRequest artistRequest = new ArtistRequest(
                firebaseUid,
                stageName,
                genre,
                bio,
                socialLinks
        );

        ApiService api = ApiClient.getApiService(idToken);

        api.createArtist(artistRequest).enqueue(new Callback<ArtistResponse>() {
            @Override
            public void onResponse(Call<ArtistResponse> call, Response<ArtistResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showSuccessDialog();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("API_ERROR", errorBody);
                        Toast.makeText(CreateArtistActivity.this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ArtistResponse> call, Throwable t) {
                Log.e("API_FAIL", "Fallo: " + t.getMessage());
                Toast.makeText(CreateArtistActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Artista creado")
                .setMessage("Tu perfil de artista se ha creado exitosamente.")
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();
                    // 🔹 Ir a MenuActivity
                    Intent intent = new Intent(this, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish(); // Cierra la actividad actual
                })
                .show();
    }

}
