package com.music.sonart.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.sonart.R;
import com.music.sonart.model.Song;

import java.util.List;

public class SongManageAdapter extends RecyclerView.Adapter<SongManageAdapter.ManageViewHolder> {

    private List<Song> songs;
    private final OnSongManageListener listener;

    public interface OnSongManageListener {
        void onEdit(Song song);
        void onDelete(Song song);
    }

    public SongManageAdapter(List<Song> songs, OnSongManageListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_manage, parent, false);
        return new ManageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ManageViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvGenre.setText(song.getGenre() != null ? song.getGenre() : "Sin género");

        Glide.with(holder.itemView.getContext())
                .load(song.getCoverUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.ivCover);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(song));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(song));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public void updateSongs(List<Song> newSongs) {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    static class ManageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvGenre;
        ImageButton btnEdit, btnDelete;

        public ManageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvGenre = itemView.findViewById(R.id.tvGenre);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
