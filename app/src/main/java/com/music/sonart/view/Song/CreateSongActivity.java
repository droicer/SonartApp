package com.music.sonart.view.Song;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.databinding.ActivityCreateSongBinding;
import com.music.sonart.model.Song.SongResponse;
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

public class CreateSongActivity extends AppCompatActivity {

    private ActivityCreateSongBinding binding;
    private String idToken;
    private Uri songFileUri;
    private Uri coverImageUri;
    private ActivityResultLauncher<String> songFilePicker;
    private ActivityResultLauncher<String> coverImagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateSongBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize file pickers
        songFilePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                songFileUri = uri;
                String fileName = getFileName(uri);
                binding.tvSongFileName.setText(fileName != null ? fileName : "Archivo seleccionado");
                updateCreateButtonState();
            }
        });

        coverImagePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                coverImageUri = uri;
                String fileName = getFileName(uri);
                binding.tvCoverFileName.setText(fileName != null ? fileName : "Imagen seleccionada");

                // Mostrar vista previa de la imagen
                Glide.with(this)
                        .load(uri)
                        .centerCrop()
                        .into(binding.ivCoverPreview);

                updateCreateButtonState();
            }
        });

        // Configure back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Configure card selection listeners (en lugar de botones)
        binding.cardSelectSongFile.setOnClickListener(v -> {
            // Acepta MP3, WAV, OGG según las validaciones del backend
            songFilePicker.launch("audio/*");
        });

        binding.cardSelectCoverImage.setOnClickListener(v -> {
            coverImagePicker.launch("image/*");
        });

        // Configure create button
        binding.btnCreateSong.setOnClickListener(v -> {
            if (validateInputs()) {
                createSong();
            }
        });

        // Add text change listeners para validación en tiempo real
        binding.etSongTitle.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCreateButtonState();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Get Firebase token
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener(result -> {
                idToken = result.getToken();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show();
                Log.e("CreateSongActivity", "Token error: " + e.getMessage());
            });
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Actualiza el estado del botón crear según las validaciones
     */
    private void updateCreateButtonState() {
        String title = binding.etSongTitle.getText().toString().trim();
        boolean isValid = !title.isEmpty() && songFileUri != null;

        binding.btnCreateSong.setEnabled(isValid);
        binding.btnCreateSong.setAlpha(isValid ? 1.0f : 0.5f);
    }

    /**
     * Valida los inputs según las reglas del backend
     */
    private boolean validateInputs() {
        String title = binding.etSongTitle.getText().toString().trim();
        String genre = binding.etSongGenre.getText().toString().trim();

        // Validar título (required, max:150)
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

        // Validar género (nullable, max:50)
        if (!genre.isEmpty() && genre.length() > 50) {
            binding.etSongGenre.setError("El género no puede exceder 50 caracteres");
            binding.etSongGenre.requestFocus();
            return false;
        }

        // Validar archivo de canción (required)
        if (songFileUri == null) {
            Toast.makeText(this, "Por favor selecciona un archivo de canción", Toast.LENGTH_SHORT).show();
            return false;
        }

        // La imagen de portada es opcional (nullable)

        return true;
    }

    private void createSong() {
        if (idToken == null) {
            Toast.makeText(this, "Esperando token de autenticación...", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        String firebaseUid = user.getUid();
        String title = binding.etSongTitle.getText().toString().trim();
        String genre = binding.etSongGenre.getText().toString().trim();

        // Show progress bar
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvUploadProgress.setVisibility(View.VISIBLE);
        binding.tvUploadProgress.setText("Subiendo... 0%");
        binding.btnCreateSong.setEnabled(false);
        binding.cardSelectSongFile.setEnabled(false);
        binding.cardSelectCoverImage.setEnabled(false);

        // Prepare multipart request
        RequestBody firebaseUidBody = RequestBody.create(MediaType.parse("text/plain"), firebaseUid);
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);

        // El género es opcional
        RequestBody genreBody = null;
        if (!genre.isEmpty()) {
            genreBody = RequestBody.create(MediaType.parse("text/plain"), genre);
        }

        // Convert URI to file
        File songFile = uriToFile(songFileUri, "song_" + System.currentTimeMillis() + ".mp3");

        if (songFile == null) {
            hideProgress();
            Toast.makeText(this, "Error al procesar el archivo de canción", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestBody songFileBody = RequestBody.create(MediaType.parse("audio/mpeg"), songFile);
        MultipartBody.Part songFilePart = MultipartBody.Part.createFormData("file", songFile.getName(), songFileBody);

        // La imagen de portada es opcional
        MultipartBody.Part coverImagePart = null;
        if (coverImageUri != null) {
            File coverFile = uriToFile(coverImageUri, "cover_" + System.currentTimeMillis() + ".jpg");
            if (coverFile != null) {
                RequestBody coverFileBody = RequestBody.create(MediaType.parse("image/*"), coverFile);
                coverImagePart = MultipartBody.Part.createFormData("cover", coverFile.getName(), coverFileBody);
            }
        }

        // Make API call
        ApiService api = ApiClient.getApiService(idToken);

        Call<SongResponse> call;
        if (genreBody != null && coverImagePart != null) {
            call = api.createSong(firebaseUidBody, titleBody, genreBody, songFilePart, coverImagePart);
        } else if (genreBody != null) {
            call = api.createSong(firebaseUidBody, titleBody, genreBody, songFilePart, null);
        } else if (coverImagePart != null) {
            call = api.createSong(firebaseUidBody, titleBody, null, songFilePart, coverImagePart);
        } else {
            call = api.createSong(firebaseUidBody, titleBody, null, songFilePart, null);
        }

        call.enqueue(new Callback<SongResponse>() {
            @Override
            public void onResponse(Call<SongResponse> call, Response<SongResponse> response) {
                hideProgress();

                if (response.isSuccessful() && response.body() != null && response.body().getSong() != null) {
                    showSuccessDialog(response.body().getMessage());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error desconocido";
                        Log.e("CreateSongActivity", "API error: " + errorBody);
                        Toast.makeText(CreateSongActivity.this, "Error al crear canción: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("CreateSongActivity", "Error parsing response: " + e.getMessage());
                        Toast.makeText(CreateSongActivity.this, "Error al procesar respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<SongResponse> call, Throwable t) {
                hideProgress();
                Log.e("CreateSongActivity", "Network failure: " + t.getMessage());
                Toast.makeText(CreateSongActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void hideProgress() {
        binding.progressBar.setVisibility(View.GONE);
        binding.tvUploadProgress.setVisibility(View.GONE);
        binding.btnCreateSong.setEnabled(true);
        binding.cardSelectSongFile.setEnabled(true);
        binding.cardSelectCoverImage.setEnabled(true);
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("¡Canción creada!")
                .setMessage(message != null ? message : "Tu canción ha sido creada exitosamente")
                .setCancelable(false)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    dialog.dismiss();

                    // 🔹 Ir a ArtistActivity
                    Intent intent = new Intent(CreateSongActivity.this, ArtistActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                    finish(); // 🔹 Cierra CreateSongActivity
                })
                .show();
    }


    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex != -1) {
                            result = cursor.getString(nameIndex);
                        }
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
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private File uriToFile(Uri uri, String fileName) {
        try {
            // Detectar tipo MIME
            String mimeType = getContentResolver().getType(uri);
            boolean isImage = mimeType != null && mimeType.startsWith("image/");

            if (isImage) {
                // 🔹 Comprimir si es imagen
                return compressImageFile(uri, fileName, 2 * 1024 * 1024); // Máx. 2 MB
            } else {
                // 🔹 Copiar archivo normal (mp3, etc.)
                File file = new File(getCacheDir(), fileName);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                FileOutputStream outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                inputStream.close();
                outputStream.close();
                return file;
            }

        } catch (Exception e) {
            Log.e("CreateSongActivity", "Error al convertir URI a archivo: " + e.getMessage());
            return null;
        }
    }

    /**
     * 🔹 Comprime una imagen reduciendo calidad y resolución hasta quedar debajo del tamaño máximo (ej. 2 MB)
     */
    private File compressImageFile(Uri imageUri, String outputName, long maxBytes) {
        try {
            // Decodificar imagen en Bitmap
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e("CreateSongActivity", "No se pudo decodificar la imagen");
                return null;
            }

            // 🔹 Reducir resolución si es demasiado grande
            int maxWidth = 1080;
            int maxHeight = 1080;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            if (width > maxWidth || height > maxHeight) {
                float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
                width = Math.round(width * ratio);
                height = Math.round(height * ratio);
                bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, width, height, true);
            }

            // 🔹 Guardar imagen con compresión progresiva
            File outputFile = new File(getCacheDir(), outputName);
            int quality = 95;
            FileOutputStream fos = new FileOutputStream(outputFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();

            // 🔹 Reducir calidad si aún es muy grande
            while (outputFile.length() > maxBytes && quality > 10) {
                quality -= 10;
                FileOutputStream retryFos = new FileOutputStream(outputFile);
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, retryFos);
                retryFos.flush();
                retryFos.close();
            }

            Log.d("CreateSongActivity", "Imagen comprimida: " + (outputFile.length() / 1024) + " KB | calidad " + quality);
            return outputFile;

        } catch (Exception e) {
            Log.e("CreateSongActivity", "Error al comprimir imagen: " + e.getMessage());
            return null;
        }
    }

}