package com.nagendra.nagendra.adapters;

public class BannerItem {
    private String title;
    private String image;
    private String details;

    public BannerItem(String title, String image, String details) {
        this.title = title;
        this.image = image;
        this.details = details;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getDetails() {
        return details;
    }
}
