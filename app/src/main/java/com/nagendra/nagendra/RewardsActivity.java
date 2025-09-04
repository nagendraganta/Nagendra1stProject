package com.nagendra.nagendra;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RewardsActivity extends AppCompatActivity {

    private LinearLayout rewardsContainer;
    private List<Reward> rewardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        rewardsContainer = findViewById(R.id.rewardsContainer);
        rewardList = new ArrayList<>();

        loadRewardsFromAPI();
    }

    private void loadRewardsFromAPI() {
        String url = "http://54.87.73.6/admin/get_rewards.php";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    rewardList.clear();
                    rewardsContainer.removeAllViews();

                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject obj = response.getJSONObject(i);
                            String title = obj.getString("title");
                            String amount = obj.getString("amount");
                            String link = obj.getString("link");
                            String logoUrl = obj.getString("logo");

                            String description = obj.optString("description");

                            Reward reward = new Reward(title, amount, link, logoUrl);
                            reward.setDescription(description);
                            rewardList.add(reward);
                        }

                        populateRewards();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse rewards", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to load rewards", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void populateRewards() {
        LayoutInflater inflater = LayoutInflater.from(this);

        for (Reward reward : rewardList) {
            // Use View to avoid ClassCastException if root is CardView
            View box = inflater.inflate(R.layout.reward_box, rewardsContainer, false);

            setupRewardBox(box, reward);
            rewardsContainer.addView(box);
        }
    }

    private void setupRewardBox(View box, Reward reward) {
        ImageView logo = box.findViewById(R.id.logo);
        TextView title = box.findViewById(R.id.title);
        TextView amount = box.findViewById(R.id.amount);

        title.setText(reward.getTitle());
        amount.setText("+" + reward.getAmount() + " Coins");
        Glide.with(this).load(reward.getLogoUrl()).into(logo);

        // Handle click → open detail screen
        box.setOnClickListener(v -> {
            Intent intent = new Intent(this, RewardDetailActivity.class);
            intent.putExtra("title", reward.getTitle());
            intent.putExtra("amount", reward.getAmount());
            intent.putExtra("link", reward.getLink());
            intent.putExtra("logoUrl", reward.getLogoUrl());
            intent.putExtra("description", reward.getDescription());
            startActivity(intent);
        });
    }
}
