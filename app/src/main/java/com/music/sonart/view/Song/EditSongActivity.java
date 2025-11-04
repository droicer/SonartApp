package com.music.sonart.view.Song;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.databinding.ActivityCreateSongBinding;
import com.music.sonart.model.SongResponse;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;
import com.music.sonart.view.Artist.ArtistActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditSongActivity extends AppCompatActivity {

    private ActivityCreateSongBinding binding;
    private String idToken;
    private Uri songFileUri;
    private Uri coverImageUri;
    private ActivityResultLauncher<String> songFilePicker;
    private ActivityResultLauncher<String> coverImagePicker;

    private int songId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateSongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener datos de la canción desde Intent (nombres corregidos)
        songId = getIntent().getIntExtra("song_id", 0);
        String title = getIntent().getStringExtra("title");
        String genre = getIntent().getStringExtra("genre");
        String coverUrl = getIntent().getStringExtra("coverUrl");

        binding.etSongTitle.setText(title);
        binding.etSongGenre.setText(genre);
        Glide.with(this).load(coverUrl).centerCrop().into(binding.ivCoverPreview);

        binding.title.setText("Actualizar canción");
        binding.btnCreateSong.setText("Actualizar canción");
        binding.btnCreateSong.setEnabled(false); // deshabilitado hasta obtener token

        // Inicializar pickers
        songFilePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                songFileUri = uri;
                binding.tvSongFileName.setText(getFileName(uri));
            }
        });

        coverImagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                coverImageUri = uri;
                binding.tvCoverFileName.setText(getFileName(uri));
                Glide.with(this).load(uri).centerCrop().into(binding.ivCoverPreview);
            }
        });

        // Selección de archivos
        binding.cardSelectSongFile.setOnClickListener(v -> songFilePicker.launch("audio/*"));
        binding.cardSelectCoverImage.setOnClickListener(v -> coverImagePicker.launch("image/*"));

        // Botón actualizar
        binding.btnCreateSong.setOnClickListener(v -> {
            if (validateInputs()) updateSong();
        });

        // Botón volver
        binding.btnBack.setOnClickListener(v -> finish());

        // Obtener token Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener(result -> {
                idToken = result.getToken();
                binding.btnCreateSong.setEnabled(true); // habilitar botón
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show();
                Log.e("EditSongActivity", "Token error: " + e.getMessage());
            });
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean validateInputs() {
        String title = binding.etSongTitle.getText().toString().trim();
        String genre = binding.etSongGenre.getText().toString().trim();

        if (title.isEmpty()) {
            binding.etSongTitle.setError("El título es obligatorio");
            binding.etSongTitle.requestFocus();
            return false;
        }
        if (title.length() > 150) {
            binding.etSongTitle.setError("El título no puede exceder 150 caracteres");
            binding.etSongTitle.requestFocus();
            return false;
        }
        if (!genre.isEmpty() && genre.length() > 50) {
            binding.etSongGenre.setError("El género no puede exceder 50 caracteres");
            binding.etSongGenre.requestFocus();
            return false;
        }
        return true;
    }

    private void updateSong() {
        if (idToken == null) {
            Toast.makeText(this, "Esperando token de autenticación...", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), binding.etSongTitle.getText().toString().trim());
        RequestBody genreBody = RequestBody.create(MediaType.parse("text/plain"), binding.etSongGenre.getText().toString().trim());

        MultipartBody.Part filePart = null;
        if (songFileUri != null) {
            File songFile = uriToFile(songFileUri, "song_" + System.currentTimeMillis() + ".mp3");
            if (songFile != null) {
                RequestBody songFileBody = RequestBody.create(MediaType.parse("audio/mpeg"), songFile);
                filePart = MultipartBody.Part.createFormData("file", songFile.getName(), songFileBody);
            }
        }

        MultipartBody.Part coverPart = null;
        if (coverImageUri != null) {
            File coverFile = uriToFile(coverImageUri, "cover_" + System.currentTimeMillis() + ".jpg");
            if (coverFile != null) {
                RequestBody coverBody = RequestBody.create(MediaType.parse("image/*"), coverFile);
                coverPart = MultipartBody.Part.createFormData("cover", coverFile.getName(), coverBody);
            }
        }

        binding.progressBar.setVisibility(android.view.View.VISIBLE);
        binding.btnCreateSong.setEnabled(false);

        ApiService api = ApiClient.getApiService(idToken);
        Call<SongResponse> call = api.updateSong(songId, titleBody, genreBody, filePart, coverPart);

        call.enqueue(new Callback<SongResponse>() {
            @Override
            public void onResponse(Call<SongResponse> call, Response<SongResponse> response) {
                binding.progressBar.setVisibility(android.view.View.GONE);
                binding.btnCreateSong.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(EditSongActivity.this, "Canción actualizada", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditSongActivity.this, ArtistActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                }


                else {
                    Toast.makeText(EditSongActivity.this, "Error al actualizar canción", Toast.LENGTH_SHORT).show();
                    Log.e("EditSongActivity", "Error response: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SongResponse> call, Throwable t) {
                binding.progressBar.setVisibility(android.view.View.GONE);
                binding.btnCreateSong.setEnabled(true);
                Toast.makeText(EditSongActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EditSongActivity", "Network failure: " + t.getMessage());
            }
        });
    }

    // 🔹 Métodos auxiliares
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) result = cursor.getString(nameIndex);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private File uriToFile(Uri uri, String fileName) {
        try {
            String mimeType = getContentResolver().getType(uri);
            boolean isImage = mimeType != null && mimeType.startsWith("image/");
            if (isImage) return compressImageFile(uri, fileName, 2 * 1024 * 1024);

            File file = new File(getCacheDir(), fileName);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, bytesRead);
            inputStream.close();
            outputStream.close();
            return file;

        } catch (Exception e) {
            Log.e("EditSongActivity", "Error al convertir URI a archivo: " + e.getMessage());
            return null;
        }
    }

    private File compressImageFile(Uri imageUri, String outputName, long maxBytes) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) return null;

            int maxWidth = 1080, maxHeight = 1080;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > maxWidth || height > maxHeight) {
                float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
                width = Math.round(width * ratio);
                height = Math.round(height * ratio);
                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            File outputFile = new File(getCacheDir(), outputName);
            int quality = 95;
            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();

            while (outputFile.length() > maxBytes && quality > 10) {
                quality -= 10;
                FileOutputStream retryFos = new FileOutputStream(outputFile);
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, retryFos);
                retryFos.flush();
                retryFos.close();
            }
            return outputFile;

        } catch (Exception e) {
            Log.e("EditSongActivity", "Error al comprimir imagen: " + e.getMessage());
            return null;
        }
    }
}
