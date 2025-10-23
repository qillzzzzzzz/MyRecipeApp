package com.example.myrecipeneww;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class MyRecipeActivity extends AppCompatActivity {

    private RecyclerView rvMyRecipes;
    private RecipeAdapter recipeAdapter;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipe);

        rvMyRecipes = findViewById(R.id.rvMyRecipes);
        firebaseHelper = new FirebaseHelper();

        recipeAdapter = new RecipeAdapter(new ArrayList<>(), this, firebaseHelper);
        rvMyRecipes.setAdapter(recipeAdapter);
        rvMyRecipes.setLayoutManager(new LinearLayoutManager(this));

        loadMyRecipes();
    }

    private void loadMyRecipes() {
        firebaseHelper.getAllRecipesForCurrentUser(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Recipe> recipes = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Recipe recipe = child.getValue(Recipe.class);
                    if (recipe != null) recipes.add(recipe);
                }
                recipeAdapter.setData(recipes);

                if (recipes.isEmpty()) {
                    Toast.makeText(MyRecipeActivity.this, "Belum ada resep", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyRecipeActivity.this, "Gagal load resep", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
