package com.music.sonart.view.User; // ajusta a tu paquete

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.music.sonart.R;
import com.music.sonart.fragment.MiniPlayerFragment;
import com.music.sonart.model.User.UserResponse;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.model.User.UpdateProfileResponse;
import com.music.sonart.view.Artist.ArtistActivity;
import com.music.sonart.view.Artist.CreateArtistActivity;

import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1001;
    private ImageView ivProfilePhoto;
    private TextView tvEmail;
    private EditText etName;
    private Button btnChangePhoto, btnSave, btnCreateArtist;
    private Uri selectedImageUri = null;
    private String idToken;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnBack = findViewById(R.id.btnBack);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        tvEmail = findViewById(R.id.tvEmail);
        etName = findViewById(R.id.etName);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSave);
        btnCreateArtist = findViewById(R.id.btnCreateArtist);

        FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                idToken = task.getResult().getToken();
                loadProfile();
            } else {
                Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar botón de retroceso
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MenuActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnChangePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Seleccionar imagen"), PICK_IMAGE);
        });

        btnSave.setOnClickListener(v -> updateProfile());
        btnCreateArtist.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, CreateArtistActivity.class);
            startActivity(intent);
        });

        // MINI REPRODUCTOR EN PERFIL
        if (findViewById(R.id.mini_player_container) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mini_player_container, new MiniPlayerFragment())
                    .commit();
        }

    }

    private void loadProfile() {
        ApiService api = ApiClient.getApiService(idToken);
        Call<UserResponse> call = api.getProfile();
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserResponse u = response.body();
                    Log.d("API_PROFILE", "✅ Respuesta completa: " + new Gson().toJson(u));

                    etName.setText(u.getName());
                    tvEmail.setText(u.getEmail());
                    if (u.getProfilePhoto() != null && !u.getProfilePhoto().isEmpty()) {
                        Glide.with(ProfileActivity.this).load(u.getProfilePhoto()).into(ivProfilePhoto);
                    }

                    // ✅ Verificar si el usuario ya tiene perfil de artista
                    if (u.getArtist() != null) {
                        // Tiene perfil de artista
                        btnCreateArtist.setText("Perfil de artista");
                        btnCreateArtist.setOnClickListener(v -> {
                            Intent intent = new Intent(ProfileActivity.this, ArtistActivity.class);
                            intent.putExtra("artist_data", new Gson().toJson(u.getArtist())); // opcional
                            startActivity(intent);
                        });
                    } else {
                        // No tiene perfil aún
                        btnCreateArtist.setText("Crear artista");
                        btnCreateArtist.setOnClickListener(v -> {
                            Intent intent = new Intent(ProfileActivity.this, CreateArtistActivity.class);
                            startActivity(intent);
                        });
                    }

                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "sin cuerpo";
                        Log.e("API_PROFILE", "❌ Error " + response.code() + ": " + error);
                    } catch (Exception e) {
                        Log.e("API_PROFILE", "❌ Error desconocido al leer respuesta", e);
                    }
                    Toast.makeText(ProfileActivity.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("API_PROFILE", "💥 Error de red: " + t.getMessage(), t);
                Toast.makeText(ProfileActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProfilePhoto.setImageURI(selectedImageUri);
        }
    }

    private void updateProfile() {
        ApiService api = ApiClient.getApiService(idToken);

        String nameStr = etName.getText().toString().trim();
        String firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        RequestBody uidPart = RequestBody.create(firebaseUid, MediaType.parse("text/plain"));
        RequestBody namePart = RequestBody.create(nameStr, MediaType.parse("text/plain"));

        MultipartBody.Part photoPart = null;
        if (selectedImageUri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                RequestBody reqFile = RequestBody.create(bytes, MediaType.parse(getContentResolver().getType(selectedImageUri)));
                photoPart = MultipartBody.Part.createFormData("profile_photo", "profile.jpg", reqFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Call<UpdateProfileResponse> call = api.updateProfile(uidPart, namePart, photoPart);
        call.enqueue(new Callback<UpdateProfileResponse>() {
            @Override
            public void onResponse(Call<UpdateProfileResponse> call, Response<UpdateProfileResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Perfil actualizado", Toast.LENGTH_SHORT).show();
                    loadProfile();
                } else {
                    Toast.makeText(ProfileActivity.this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateProfileResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
