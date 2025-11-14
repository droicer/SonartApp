package com.music.sonart.adapter.Song;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.sonart.R;
import com.music.sonart.model.Song.SongStatsResponse;

import java.util.ArrayList;
import java.util.List;

public class SongStatsAdapter extends RecyclerView.Adapter<SongStatsAdapter.ViewHolder> {

    private List<SongStatsResponse.SongStats> songList;

    public SongStatsAdapter() {
        this.songList = new ArrayList<>();
    }

    public void updateData(List<SongStatsResponse.SongStats> newList) {
        this.songList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewTypeりたい) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_statistic, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongStatsResponse.SongStats song = songList.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvLikes.setText(String.valueOf(song.getLikesCount()));
        holder.tvPlays.setText(String.valueOf(song.getPlaysCount()));

        Glide.with(holder.itemView.getContext())
                .load(song.getCoverUrl())
                .placeholder(R.drawable.placeholder_cover)
                .error(R.drawable.placeholder_cover)
                .into(holder.ivCover);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvArtist, tvLikes, tvPlays;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvPlays = itemView.findViewById(R.id.tvPlays);
        }
    }
}