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
import com.music.sonart.adapter.Song.SongAdapter;
import com.music.sonart.model.Search.SearchResponse;
import com.music.sonart.model.Song.Song;
import com.music.sonart.view.Song.PlaySongActivity;

import java.util.ArrayList;
import java.util.List;

public class SearchSongsFragment extends Fragment {

    private static final String ARG_SONGS = "songs";
    private List<SearchResponse.Song> songs = new ArrayList<>();

    public static SearchSongsFragment newInstance(List<SearchResponse.Song> songs) {
        SearchSongsFragment fragment = new SearchSongsFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_SONGS, new ArrayList<>(songs));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            songs = getArguments().getParcelableArrayList(ARG_SONGS);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_songs, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // === CONVERTIR SearchResponse.Song → Song ===
        List<Song> songList = new ArrayList<>();
        for (SearchResponse.Song s : songs) {
            Song song = new Song();
            song.setArtistId(s.artist.id);
            song.setId(s.id);
            song.setTitle(s.title);
            song.setArtistName(s.artist.stageName);
            song.setCoverUrl(s.coverUrl);
            song.setFileUrl(s.fileUrl);
            songList.add(song);
        }

        // === USAR TU SongAdapter ===
        SongAdapter adapter = new SongAdapter(songList, song -> {
            Intent intent = new Intent(getActivity(), PlaySongActivity.class);
            intent.putExtra("song", song);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        return view;
    }
}
