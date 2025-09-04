package com.nagendra.nagendra;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MyRewardsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RewardHistoryAdapter adapter;
    private ArrayList<RewardModel> rewardList;
    private TextView tvNoHistory;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reward);

        recyclerView = findViewById(R.id.recyclerViewRewards);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tvNoHistory = findViewById(R.id.tvNoHistory); // TextView to show "No history"

        rewardList = new ArrayList<>();
        adapter = new RewardHistoryAdapter(this, rewardList);
        recyclerView.setAdapter(adapter);

        // Fetch user_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", 0); // default 0 if not found

        if (userId == 0) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            tvNoHistory.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh reward list every time activity resumes
        if (userId != 0) {
            fetchRewardHistory(userId);
        }
    }

    private void fetchRewardHistory(int userId) {
        String url = "http://54.87.73.6/reward_history.php?user_id=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    rewardList.clear();

                    try {
                        boolean success = response.getBoolean("success");
                        if (!success || response.getInt("count") == 0) {
                            // No rewards
                            tvNoHistory.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                            return;
                        }

                        // Hide "No history" TextView if data exists
                        tvNoHistory.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        if (response.has("rewards")) {
                            JSONArray rewardsArray = response.getJSONArray("rewards");

                            for (int i = 0; i < rewardsArray.length(); i++) {
                                JSONObject obj = rewardsArray.getJSONObject(i);
                                String title = obj.has("reward_title") ? obj.getString("reward_title") : "";
                                String mobile = obj.has("mobile_number") ? obj.getString("mobile_number") : "";
                                String upiId = obj.has("upi_id") ? obj.getString("upi_id") : "";
                                String status = obj.has("status") ? obj.getString("status") : "";

                                rewardList.add(new RewardModel(title, mobile, upiId, status));
                            }

                            adapter.notifyDataSetChanged();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MyRewardsActivity.this, "JSON Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                },
                error -> {
                    String message = "Unknown error";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        message = new String(error.networkResponse.data);
                    } else if (error.getMessage() != null) {
                        message = error.getMessage();
                    }
                    Toast.makeText(MyRewardsActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();

                    // Show "No history" on error
                    tvNoHistory.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
        );

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
