package com.example.myrecipeneww;

public class RecipeFavorite {
    private String id;          // ID dari Firebase
    private String title;
    private String description;
    private String imageUrl;
    private boolean isFavorite;

    public RecipeFavorite() {}

    public RecipeFavorite(String id, String title, String description, String imageUrl, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isFavorite = isFavorite;
    }

    // ✅ getter & setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
