package com.music.sonart.view.Song;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.music.sonart.R;
import com.music.sonart.adapter.Search.RecentSearchAdapter;
import com.music.sonart.adapter.Search.SearchPagerAdapter;
import com.music.sonart.databinding.ActivitySearchBinding;
import com.music.sonart.fragment.MiniPlayerFragment;
import com.music.sonart.model.Search.SearchResponse;
import com.music.sonart.network.ApiClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private ActivitySearchBinding binding;
    private SearchPagerAdapter pagerAdapter;
    private RecentSearchAdapter recentAdapter;
    private List<String> recentSearches = new ArrayList<>();
    private SharedPreferences prefs;
    private String idToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("search_prefs", MODE_PRIVATE);
        loadRecentSearches();

        setupToolbar();
        setupSearch();
        setupTabs();
        setupRecentSearches();

        getFirebaseToken();

        // Mini Player
        if (findViewById(R.id.mini_player_container) != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.mini_player_container, new MiniPlayerFragment())
                    .commit();
        }
    }

    private void getFirebaseToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnSuccessListener(result -> {
                idToken = result.getToken();
                showRecentSearches(); // Mostrar recientes
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error al obtener token", Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.btnClear.setOnClickListener(v -> {
            binding.etSearch.setText("");
            showRecentSearches();
        });

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString().trim();
                if (!query.isEmpty() && idToken != null) {
                    performSearch(query);
                }
                return true;
            }
            return false;
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            String query = binding.etSearch.getText().toString().trim();
            if (!query.isEmpty() && idToken != null) {
                performSearch(query);
            } else {
                binding.swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void performSearch(String query) {
        saveToRecent(query);
        showLoading();

        ApiClient.getApiService(idToken).search(query, 20, 1).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                binding.swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    SearchResponse.Results r = response.body().getResults();
                    if (r.songs.data.isEmpty() && r.artists.data.isEmpty()) {
                        showNoResults(query);
                    } else {
                        showResults(r.songs.data, r.artists.data);
                    }
                } else {
                    showError("Error en la respuesta");
                }
            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                binding.swipeRefresh.setRefreshing(false);
                showError("Error de red: " + t.getMessage());
            }
        });
    }

    private void setupTabs() {
        pagerAdapter = new SearchPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Canciones" : "Artistas");
        }).attach();
    }

    private void setupRecentSearches() {
        recentAdapter = new RecentSearchAdapter(recentSearches, query -> {
            binding.etSearch.setText(query);
            binding.etSearch.setSelection(query.length());
            performSearch(query);
        });
        binding.rvRecentSearches.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecentSearches.setAdapter(recentAdapter);

        // AÑADIDO: OnClickListener para "Limpiar"
        binding.btnClearHistory.setOnClickListener(v -> clearRecentSearches());
    }

    private void showResults(List<SearchResponse.Song> songs, List<SearchResponse.Artist> artists) {
        binding.layoutRecent.setVisibility(View.GONE);
        binding.layoutResults.setVisibility(View.VISIBLE);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutNoResults.setVisibility(View.GONE);

        pagerAdapter.updateData(songs, artists);
        binding.viewPager.setCurrentItem(0, false);
    }

    private void showRecentSearches() {
        binding.layoutRecent.setVisibility(View.VISIBLE);
        binding.layoutResults.setVisibility(View.GONE);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutNoResults.setVisibility(View.GONE);

        recentAdapter.updateData(recentSearches);
        binding.btnClearHistory.setVisibility(recentSearches.isEmpty() ? View.GONE : View.VISIBLE);

        // Mostrar estado vacío si no hay búsquedas
        binding.layoutEmptyRecent.setVisibility(recentSearches.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showLoading() {
        binding.layoutRecent.setVisibility(View.GONE);
        binding.layoutResults.setVisibility(View.GONE);
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.layoutNoResults.setVisibility(View.GONE);
    }

    private void showNoResults(String query) {
        binding.layoutRecent.setVisibility(View.GONE);
        binding.layoutResults.setVisibility(View.GONE);
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutNoResults.setVisibility(View.VISIBLE);
        binding.tvNoResultsMessage.setText("No encontramos resultados para \"" + query + "\"");
    }

    private void showError(String msg) {
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
    }

    // === BÚSQUEDAS RECIENTES ===
    private void saveToRecent(String query) {
        if (recentSearches.contains(query)) recentSearches.remove(query);
        recentSearches.add(0, query);
        if (recentSearches.size() > 10) recentSearches.remove(recentSearches.size() - 1);
        prefs.edit().putString("recent", String.join(",", recentSearches)).apply();
    }

    private void loadRecentSearches() {
        String saved = prefs.getString("recent", "");
        if (!saved.isEmpty()) {
            recentSearches = new ArrayList<>(Arrays.asList(saved.split(",")));
        }
    }

    private void clearRecentSearches() {
        recentSearches.clear();
        prefs.edit().remove("recent").apply();
        recentAdapter.updateData(recentSearches);
        binding.btnClearHistory.setVisibility(View.GONE);
        binding.layoutEmptyRecent.setVisibility(View.VISIBLE); // Mostrar "Busca tu música"
    }
}