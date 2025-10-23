package com.example.myrecipeneww;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class FirebaseRecipeService {
    private final DatabaseReference userRecipesRef;
    private final DatabaseReference favoritesRef;
    private final String currentUserId;

    public FirebaseRecipeService() {
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : "guest_user"; // fallback kalau belum login

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        userRecipesRef = database.getReference("users")
                .child(currentUserId)
                .child("recipes");
        favoritesRef = database.getReference("users")
                .child(currentUserId)
                .child("favorites");
    }

    // 🔹 Ambil semua resep user berdasarkan kategori
    public void getRecipesByCategory(String category, RecipeCallback callback) {
        userRecipesRef.orderByChild("category").equalTo(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Recipe> recipes = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Recipe recipe = data.getValue(Recipe.class);
                            if (recipe != null) {
                                recipe.setId(data.getKey());
                                recipes.add(recipe);
                            }
                        }
                        callback.onCallback(recipes);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        callback.onCallback(new ArrayList<>());
                    }
                });
    }

    // 🔹 Ambil resep favorit
    public void getFavoriteRecipes(RecipeCallback callback) {
        favoritesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Recipe> favorites = new ArrayList<>();
                for (DataSnapshot favIdSnap : snapshot.getChildren()) {
                    String recipeId = favIdSnap.getKey();
                    if (recipeId != null && Boolean.TRUE.equals(favIdSnap.getValue(Boolean.class))) {
                        userRecipesRef.child(recipeId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot recipeSnap) {
                                        Recipe recipe = recipeSnap.getValue(Recipe.class);
                                        if (recipe != null) {
                                            recipe.setId(recipeId);
                                            favorites.add(recipe);
                                        }
                                        callback.onCallback(favorites);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        callback.onCallback(favorites);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onCallback(new ArrayList<>());
            }
        });
    }

    // 🔹 Tambah resep baru
    public void addRecipe(Recipe recipe) {
        String key = userRecipesRef.push().getKey();
        if (key != null) {
            recipe.setId(key);
            userRecipesRef.child(key).setValue(recipe);
        }
    }

    // 🔹 Ubah status favorit
    public void updateFavoriteStatus(String recipeId, boolean isFavorite) {
        if (isFavorite) {
            favoritesRef.child(recipeId).setValue(true);
        } else {
            favoritesRef.child(recipeId).removeValue();
        }
    }

    // 🔹 Hapus resep
    public void deleteRecipe(String recipeId) {
        userRecipesRef.child(recipeId).removeValue();
        favoritesRef.child(recipeId).removeValue();
    }

    // 🔹 Callback interface
    public interface RecipeCallback {
        void onCallback(List<Recipe> recipes);
    }
}
