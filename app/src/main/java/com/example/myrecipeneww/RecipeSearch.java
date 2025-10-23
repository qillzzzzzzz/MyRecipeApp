package com.example.myrecipeneww;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class RecipeSearch {
    private String id;
    private String name;      // untuk data lama
    private String title;     // untuk data baru
    private String imageUrl;

    public RecipeSearch() {}

    public RecipeSearch(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // 🔹 Gunakan ini untuk menampilkan nama resep, otomatis ambil title kalau ada
    public String getDisplayName() {
        if (title != null && !title.isEmpty()) {
            return title;
        } else {
            return name;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
