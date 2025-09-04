package com.nagendra.nagendra;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class BannerDetailActivity extends AppCompatActivity {

    private ImageView bannerImage;
    private TextView bannerTitle, bannerDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner_detail);

        bannerImage = findViewById(R.id.detailBannerImage);
        bannerTitle = findViewById(R.id.detailBannerTitle);
        bannerDescription = findViewById(R.id.detailBannerDescription);

        String title = getIntent().getStringExtra("title");
        String image = getIntent().getStringExtra("image");
        String details = getIntent().getStringExtra("details");

        bannerTitle.setText(title);
        bannerDescription.setText(details);

        Glide.with(this)
                .load(image)
                .into(bannerImage);
    }
}
