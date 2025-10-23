package com.example.myrecipeneww;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

public class FirebaseHelper {

    private final DatabaseReference dbRef;
    private final StorageReference storageRef;

    public FirebaseHelper() {
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        storageRef = FirebaseStorage.getInstance().getReference("recipes_images");
    }

    // ======= USER PROFILE =======
    public void saveUserProfile(String uid, String name, String email, String photoUrl) {
        if (uid == null) return;
        UserProfile profile = new UserProfile(name, email, photoUrl);
        dbRef.child(uid).child("profile").setValue(profile);
    }

    public static class UserProfile {
        public String name;
        public String email;
        public String photoUrl;

        public UserProfile() {}
        public UserProfile(String name, String email, String photoUrl) {
            this.name = name;
            this.email = email;
            this.photoUrl = photoUrl;
        }
    }

    // ======= CALLBACK INTERFACE =======
    public interface SimpleCallback {
        void onSuccess();
        void onFailed(Exception e);
    }

    public interface UrlCallback {
        void onUrl(String url);
        void onFailed(Exception e);
    }

    public interface OneRecipeCallback {
        void onFound(Recipe recipe);
        void onNotFound();
        void onFailed(Exception e);
    }

    // ======= RECIPE =======
    public void getRecipeForCurrentUser(String recipeId, OneRecipeCallback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child(uid).child("recipes").child(recipeId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Recipe recipe = snapshot.getValue(Recipe.class);
                            if (recipe != null) callback.onFound(recipe);
                            else callback.onNotFound();
                        } else {
                            callback.onNotFound();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailed(error.toException());
                    }
                });
    }

    public void updateRecipeForCurrentUser(Recipe recipe, SimpleCallback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child(uid).child("recipes").child(recipe.getId())
                .setValue(recipe)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailed);
    }

    public void saveNewRecipeForCurrentUser(Uri imageUri, Recipe recipe, SimpleCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            callback.onFailed(new Exception("User belum login"));
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String newId = dbRef.child(uid).child("recipes").push().getKey();
        if (newId == null) {
            callback.onFailed(new Exception("Gagal membuat ID resep"));
            return;
        }

        recipe.setId(newId);
        if (recipe.getCreatedAt() == 0) recipe.setCreatedAt(System.currentTimeMillis());

        if (imageUri != null) {
            uploadImage(imageUri, new UrlCallback() {
                @Override
                public void onUrl(String url) {
                    recipe.setImageUrl(url);
                    uploadRecipeData(uid, recipe, callback);
                }

                @Override
                public void onFailed(Exception e) {
                    recipe.setImageUrl("");
                    uploadRecipeData(uid, recipe, callback);
                }
            });
        } else {
            recipe.setImageUrl("");
            uploadRecipeData(uid, recipe, callback);
        }
    }

    private void uploadRecipeData(String uid, Recipe recipe, SimpleCallback callback) {
        dbRef.child(uid).child("recipes").child(recipe.getId())
                .setValue(recipe)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailed);
    }

    public void uploadImage(Uri imageUri, UrlCallback callback) {
        if (imageUri == null) {
            callback.onFailed(new Exception("Image URI is null"));
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            callback.onFailed(new Exception("User belum login"));
            return;
        }

        String uid = auth.getCurrentUser().getUid();
        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(uid).child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> callback.onUrl(uri.toString()))
                                .addOnFailureListener(callback::onFailed)
                )
                .addOnFailureListener(callback::onFailed);
    }

    // ======= FAVORITE =======
    public void updateFavoriteStatus(String recipeId, boolean isFavorite, SimpleCallback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child(uid).child("recipes").child(recipeId).child("isFavorite")
                .setValue(isFavorite)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailed);
    }

    // ======= OTHER FUNCTIONS =======
    public void startCooking(Context context, String recipeId) {
        Intent intent = new Intent(context, CookRecipeActivity.class);
        intent.putExtra("recipeId", recipeId);
        context.startActivity(intent);
    }

    public void startEditRecipe(Context context, Recipe recipe) {
        Intent intent = new Intent(context, EditRecipeActivity.class);
        intent.putExtra("recipeId", recipe.getId());
        intent.putExtra("title", recipe.getTitle());
        intent.putExtra("ringkasan", recipe.getRingkasan());
        intent.putExtra("alat", recipe.getAlat());
        intent.putExtra("bahan", recipe.getBahan());
        intent.putExtra("langkah", recipe.getLangkah());
        intent.putExtra("imageUrl", recipe.getImageUrl());
        context.startActivity(intent);
    }

    public void deleteRecipeForCurrentUser(String recipeId, SimpleCallback callback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child(uid).child("recipes").child(recipeId)
                .removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onFailed);
    }

    public void getAllRecipesForCurrentUser(ValueEventListener listener) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.child(uid).child("recipes").addListenerForSingleValueEvent(listener);
    }
}
