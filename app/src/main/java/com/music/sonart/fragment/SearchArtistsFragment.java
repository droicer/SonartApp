package com.music.sonart.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.music.sonart.R;
import com.music.sonart.adapter.Search.ArtistSearchAdapter;
import com.music.sonart.model.Search.SearchResponse;
import com.music.sonart.view.Artist.ArtistProfileActivity;
import java.util.ArrayList;
import java.util.List;

public class SearchArtistsFragment extends Fragment {

    private static final String ARG_ARTISTS = "artists";
    private List<SearchResponse.Artist> artists = new ArrayList<>();

    public static SearchArtistsFragment newInstance(List<SearchResponse.Artist> artists) {
        SearchArtistsFragment fragment = new SearchArtistsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_ARTISTS, new ArrayList<>(artists));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            List<SearchResponse.Artist> received = getArguments().getParcelableArrayList(ARG_ARTISTS);
            if (received != null) {
                artists = received;
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_artists, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerArtists);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArtistSearchAdapter adapter = new ArtistSearchAdapter();
        adapter.updateData(artists);

        // CLICK → ABRIR PERFIL
        adapter.setOnArtistClickListener(artist -> {
            Intent intent = new Intent(getActivity(), ArtistProfileActivity.class);
            intent.putExtra("artist", artist); // Pasa TODO el objeto
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        return view;
    }
}