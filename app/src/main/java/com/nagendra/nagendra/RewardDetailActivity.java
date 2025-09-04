package com.nagendra.nagendra;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

public class RewardDetailActivity extends AppCompatActivity {

    private LinearLayout formLayout;
    private Button btnCompleted, btnSubmit;
    private EditText etMobile, etUpi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_detail);

        ImageView logo = findViewById(R.id.detailLogo);
        TextView title = findViewById(R.id.detailTitle);
        TextView amount = findViewById(R.id.detailAmount);
        Button openLink = findViewById(R.id.btnOpenLink);
        TextView description = findViewById(R.id.detailDescription);

        // ===== form references =====
        formLayout = findViewById(R.id.formLayout);
        btnCompleted = findViewById(R.id.btnCompleted);
        btnSubmit = findViewById(R.id.btnSubmit);
        etMobile = findViewById(R.id.etMobile);
        etUpi = findViewById(R.id.etUpi);

        // ===== intent data =====
        String rewardTitle = getIntent().getStringExtra("title");
        String rewardAmount = getIntent().getStringExtra("amount");
        String rewardLink = getIntent().getStringExtra("link");
        String logoUrl = getIntent().getStringExtra("logoUrl");
        String rewardDescription = getIntent().getStringExtra("description");

        if (rewardDescription != null) description.setText(rewardDescription);
        if (rewardTitle != null) title.setText(rewardTitle);
        if (rewardAmount != null) amount.setText("+" + rewardAmount + " Coins");

        if (logoUrl != null && !logoUrl.isEmpty()) {
            Glide.with(this).load(logoUrl).into(logo);
        } else {
            logo.setImageResource(R.drawable.ic_reward_logo);
        }

        openLink.setOnClickListener(v -> {
            if (rewardLink != null && !rewardLink.isEmpty()) {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(rewardLink)));
            }
        });

        // ===== Completed button logic =====
        btnCompleted.setOnClickListener(v -> {
            formLayout.setVisibility(LinearLayout.VISIBLE);
            btnCompleted.setVisibility(Button.GONE);
        });

        // ===== Submit logic =====
        btnSubmit.setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            String upiId = etUpi.getText().toString().trim();

            if (mobile.isEmpty() || upiId.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", 0);
            if (userId == 0) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable the submit button immediately
            btnSubmit.setEnabled(false);
            submitReward(userId, rewardTitle, mobile, upiId);
        });
    }

    private void submitReward(int userId, String rewardTitle, String mobile, String upiId) {
        String url = "http://54.87.73.6/submit_reward.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "Submission saved!", Toast.LENGTH_SHORT).show();
                    formLayout.setVisibility(LinearLayout.GONE);
                    btnCompleted.setVisibility(Button.VISIBLE);

                    // Re-enable submit button after 10 seconds
                    new Handler().postDelayed(() -> btnSubmit.setEnabled(true), 10000);

                },
                error -> {
                    Toast.makeText(this, "Submission failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    // Re-enable immediately on failure
                    btnSubmit.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("reward_title", rewardTitle);
                params.put("mobile_number", mobile);
                params.put("upi_id", upiId);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
