package com.music.sonart.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.music.sonart.R;
import com.music.sonart.model.Search.SocialLink;
import java.util.ArrayList;
import java.util.List;

public class SocialLinkAdapter extends RecyclerView.Adapter<SocialLinkAdapter.ViewHolder> {

    private List<SocialLink> links = new ArrayList<>();

    public void updateLinks(List<SocialLink> newLinks) {
        links.clear();
        links.addAll(newLinks);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_social_link, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SocialLink link = links.get(position);
        holder.tvPlatform.setText(link.platform);
        holder.tvLink.setText(link.url);
        holder.ivIcon.setImageResource(link.iconRes);

        holder.itemView.setOnClickListener(v -> {
            try {
                String finalUrl = link.url;

                //Si NO empieza con http o https, le agregamos https://
                if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                    finalUrl = "https://" + finalUrl;
                }

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public int getItemCount() { return links.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvPlatform, tvLink;

        ViewHolder(View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvPlatform = itemView.findViewById(R.id.tvPlatform);
            tvLink = itemView.findViewById(R.id.tvLink);
        }
    }
}