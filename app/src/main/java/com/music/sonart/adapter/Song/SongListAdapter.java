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
import com.music.sonart.model.Song.Song;

import java.util.List;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> {

    private List<Song> songs;
    private OnSongClickListener listener;

    // 🔹 Interface para manejar clics en una canción
    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    public SongListAdapter(List<Song> songs, OnSongClickListener listener) {
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_song_list, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtistName());
        holder.tvGenre.setText(song.getGenre() != null ? song.getGenre() : "Desconocido");

        Glide.with(holder.itemView.getContext())
                .load(song.getCoverUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.ivCover);

        // 🔹 Click en toda la tarjeta
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSongClick(song);
        });

        // 🔹 Click específico en el botón de play
        holder.ivPlay.setOnClickListener(v -> {
            if (listener != null) listener.onSongClick(song);
        });
    }

    @Override
    public int getItemCount() {
        return songs != null ? songs.size() : 0;
    }

    // 🔹 Método para actualizar canciones
    public void updateSongs(List<Song> newSongs) {
        songs.clear();
        songs.addAll(newSongs);
        notifyDataSetChanged();
    }

    // 🔹 ViewHolder
    static class SongViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover, ivPlay;
        TextView tvTitle, tvArtist, tvGenre;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            ivPlay = itemView.findViewById(R.id.ivPlay);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvGenre = itemView.findViewById(R.id.tvGenre);
        }
    }
}
