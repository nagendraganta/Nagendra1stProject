package com.nagendra.nagendra.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nagendra.nagendra.BannerDetailActivity;
import com.nagendra.nagendra.R;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private final Context context;
    private final List<BannerItem> bannerList;

    public BannerAdapter(Context context, List<BannerItem> bannerList) {
        this.context = context;
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem banner = bannerList.get(position);

        // set title
        holder.title.setText(banner.getTitle());

        // load image with Glide
        Glide.with(holder.image.getContext())
                .load(banner.getImage())
                .placeholder(R.drawable.placeholder_banner) // optional
                .error(R.drawable.error_banner)             // optional
                .into(holder.image);

        // handle click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BannerDetailActivity.class);
            intent.putExtra("title", banner.getTitle());
            intent.putExtra("image", banner.getImage());
            intent.putExtra("details", banner.getDetails());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return bannerList != null ? bannerList.size() : 0;
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.bannerImage);
            title = itemView.findViewById(R.id.bannerTitle);
        }
    }
}
