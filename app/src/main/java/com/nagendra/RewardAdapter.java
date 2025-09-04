package com.nagendra.nagendra;

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

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {

    private Context context;
    private List<Reward> rewardList;

    public RewardAdapter(Context context, List<Reward> rewardList) {
        this.context = context;
        this.rewardList = rewardList;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reward_item, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewardList.get(position);

        holder.title.setText(reward.getTitle());
        holder.amount.setText("+" + reward.getAmount() + " Coins");

        // Load logo
        Glide.with(context).load(reward.getLogoUrl()).into(holder.logo);

        // OnClick → open detail page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RewardDetailActivity.class);
            intent.putExtra("title", reward.getTitle());
            intent.putExtra("amount", reward.getAmount());
            intent.putExtra("link", reward.getLink());
            intent.putExtra("logoUrl", reward.getLogoUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    public static class RewardViewHolder extends RecyclerView.ViewHolder {
        TextView title, amount;
        ImageView logo;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            amount = itemView.findViewById(R.id.amount);
            logo = itemView.findViewById(R.id.logo);
        }
    }
}
