package com.nagendra.nagendra;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RewardHistoryAdapter extends RecyclerView.Adapter<RewardHistoryAdapter.ViewHolder> {

    private Context context;
    private List<RewardModel> rewardList;

    public RewardHistoryAdapter(Context context, List<RewardModel> rewardList) {
        this.context = context;
        this.rewardList = rewardList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reward_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RewardModel reward = rewardList.get(position);

        holder.tvTitle.setText("Reward: " + reward.getRewardTitle());
        holder.tvMobile.setText("Mobile: " + reward.getMobile());
        holder.tvUpi.setText("UPI: " + reward.getUpiId());
        holder.tvStatus.setText(reward.getStatus());

        // Change color based on status
        if (reward.getStatus().equalsIgnoreCase("Completed")) {
            holder.tvStatus.setTextColor(Color.parseColor("#388E3C")); // Green
        } else {
            holder.tvStatus.setTextColor(Color.parseColor("#FBC02D")); // Yellow
        }
    }

    @Override
    public int getItemCount() {
        return rewardList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMobile, tvUpi, tvStatus;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRewardTitle);
            tvMobile = itemView.findViewById(R.id.tvRewardMobile);
            tvUpi = itemView.findViewById(R.id.tvRewardUpi);
            tvStatus = itemView.findViewById(R.id.tvRewardStatus);
            cardView = itemView.findViewById(R.id.rewardCard);
        }
    }
}
