package com.music.sonart.adapter.Search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.music.sonart.R;
import com.music.sonart.model.Search.SearchResponse;

import java.util.ArrayList;
import java.util.List;

public class ArtistSearchAdapter extends RecyclerView.Adapter<ArtistSearchAdapter.ViewHolder> {

    private List<SearchResponse.Artist> artists = new ArrayList<>();
    private OnArtistClickListener listener;

    public ArtistSearchAdapter() {}

    public void updateData(List<SearchResponse.Artist> newArtists) {
        this.artists = newArtists != null ? newArtists : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResponse.Artist artist = artists.get(position);

        holder.tvName.setText(artist.stageName);
        holder.tvGenre.setText(artist.genre != null ? artist.genre : "Sin género");

        // Verificado
        holder.ivVerified.setVisibility(artist.verified ? View.VISIBLE : View.GONE);

        // Foto del artista
        Glide.with(holder.itemView.getContext())
                .load(artist.profileImage)
                .placeholder(R.drawable.error_image)
                .error(R.drawable.placeholder_image)
                .circleCrop()
                .into(holder.ivPhoto);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArtistClick(artist);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    // === VIEW HOLDER ===
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto, ivVerified;
        TextView tvName, tvGenre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivArtistPhoto);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            tvName = itemView.findViewById(R.id.tvArtistName);
            tvGenre = itemView.findViewById(R.id.tvGenre);
        }
    }

    // === CLICK LISTENER ===
    public interface OnArtistClickListener {
        void onArtistClick(SearchResponse.Artist artist);
    }

    public void setOnArtistClickListener(OnArtistClickListener listener) {
        this.listener = listener;
    }
}