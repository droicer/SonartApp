package com.music.sonart.view.Song;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.adapter.Song.SongStatsAdapter;
import com.music.sonart.databinding.ActivityStatisticsBinding;
import com.music.sonart.model.Song.SongStatsResponse;
import com.music.sonart.network.ApiClient;
import com.music.sonart.network.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StatisticsActivity extends AppCompatActivity {

    private ActivityStatisticsBinding binding;
    private SongStatsAdapter adapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 🔙 Botón volver
        binding.btnBack.setOnClickListener(v -> finish());

        // 🎵 RecyclerView
        binding.recyclerStats.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongStatsAdapter();
        binding.recyclerStats.setAdapter(adapter);

        // 🔄 Botón reintentar
        binding.btnRetry.setOnClickListener(v -> loadStatistics());

        // 🔐 Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // 🚀 Cargar estadísticas
        loadStatistics();
    }

    private void loadStatistics() {
        showLoading(true);
        showEmptyState(false);
        showErrorState(false, "");

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showLoading(false);
            showError("No estás autenticado");
            return;
        }

        // Obtener token (cacheado)
        currentUser.getIdToken(false)
                .addOnSuccessListener(result -> {
                    String token = result.getToken();
                    String firebaseUid = currentUser.getUid();
                    fetchStatsFromApi(firebaseUid, token);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    showError("Error al autenticar usuario");
                    Log.e("Statistics", "Error al obtener token", e);
                });
    }

    private void fetchStatsFromApi(String firebaseUid, String token) {
        ApiService api = ApiClient.getApiService(token);

        api.getSongStats(firebaseUid).enqueue(new Callback<SongStatsResponse>() {
            @Override
            public void onResponse(Call<SongStatsResponse> call, Response<SongStatsResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    SongStatsResponse data = response.body();

                    if (data.getSongs() == null || data.getSongs().isEmpty()) {
                        showEmptyState(true);
                    } else {
                        adapter.updateData(data.getSongs());
                        showEmptyState(false);
                    }

                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(Call<SongStatsResponse> call, Throwable t) {
                showLoading(false);
                showErrorState(true, "Error de conexión: " + t.getMessage());
                Log.e("Statistics", "Error de red: ", t);
            }
        });
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
            Log.e("API_ERROR", "Código: " + response.code() + " | " + errorBody);
        } catch (Exception e) {
            Log.e("API_ERROR", "No se pudo leer el error", e);
        }
        showErrorState(true, "No se pudieron cargar las estadísticas");
    }

    // ─── Estados visuales ───────────────────────────────

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerStats.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    private void showEmptyState(boolean show) {
        binding.layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerStats.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.layoutErrorState.setVisibility(View.GONE);
    }

    private void showErrorState(boolean show, String message) {
        binding.layoutErrorState.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerStats.setVisibility(show ? View.GONE : View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.tvErrorMessage.setText(message);
    }

    private void showError(String message) {
        Toast.makeText(this, "⚠️ " + message, Toast.LENGTH_SHORT).show();
    }
}
