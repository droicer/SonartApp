package com.music.sonart.adapter.Search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.music.sonart.R;
import java.util.ArrayList;
import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {

    private List<String> queries = new ArrayList<>();
    private OnRecentClickListener listener;

    public interface OnRecentClickListener {
        void onClick(String query);
    }

    public RecentSearchAdapter(List<String> queries, OnRecentClickListener listener) {
        this.queries = new ArrayList<>(queries);
        this.listener = listener;
    }

    public void updateData(List<String> newQueries) {
        queries.clear();
        queries.addAll(newQueries);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String query = queries.get(position);
        holder.tvQuery.setText(query);
        holder.itemView.setOnClickListener(v -> listener.onClick(query));
    }

    @Override
    public int getItemCount() {
        return queries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuery;
        ViewHolder(View itemView) {
            super(itemView);
            tvQuery = itemView.findViewById(R.id.tvQuery);
        }
    }
}