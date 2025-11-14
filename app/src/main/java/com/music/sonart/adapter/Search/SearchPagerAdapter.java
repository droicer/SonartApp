package com.music.sonart.adapter.Search;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.music.sonart.fragment.SearchArtistsFragment;
import com.music.sonart.fragment.SearchSongsFragment;
import com.music.sonart.model.Search.SearchResponse;

import java.util.ArrayList;
import java.util.List;

public class SearchPagerAdapter extends FragmentStateAdapter {

    private List<SearchResponse.Song> currentSongs = new ArrayList<>();
    private List<SearchResponse.Artist> currentArtists = new ArrayList<>();

    private long dataVersion = 0; // 🔥 cambia cuando se nueva búsqueda

    public SearchPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void updateData(List<SearchResponse.Song> songs, List<SearchResponse.Artist> artists) {
        this.currentSongs = songs != null ? new ArrayList<>(songs) : new ArrayList<>();
        this.currentArtists = artists != null ? new ArrayList<>(artists) : new ArrayList<>();

        dataVersion++;     // 🔥 avisamos que es "datos nuevos"
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return SearchSongsFragment.newInstance(currentSongs);
        } else {
            return SearchArtistsFragment.newInstance(currentArtists);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return dataVersion * 10 + position;
    }

    @Override
    public boolean containsItem(long itemId) {
        long pos0 = dataVersion * 10;
        long pos1 = dataVersion * 10 + 1;
        return itemId == pos0 || itemId == pos1;
    }
}
