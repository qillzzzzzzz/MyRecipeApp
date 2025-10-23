package com.example.myrecipeneww;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapterSearch extends RecyclerView.Adapter<RecipeAdapterSearch.RecipeViewHolder> {

    private List<RecipeSearch> recipeList;
    private List<RecipeSearch> recipeListFull;

    public RecipeAdapterSearch(List<RecipeSearch> recipeList) {
        this.recipeList = recipeList;
        this.recipeListFull = new ArrayList<>(recipeList);
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_search, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeSearch recipe = recipeList.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // 🔍 Filter berdasarkan title atau name
    public void filter(String searchText) {
        List<RecipeSearch> filteredList = new ArrayList<>();

        if (searchText.isEmpty()) {
            filteredList.addAll(recipeListFull);
        } else {
            String filterPattern = searchText.toLowerCase().trim();
            for (RecipeSearch recipe : recipeListFull) {
                if (recipe.getDisplayName() != null &&
                        recipe.getDisplayName().toLowerCase().contains(filterPattern)) {
                    filteredList.add(recipe);
                }
            }
        }

        recipeList.clear();
        recipeList.addAll(filteredList);
        notifyDataSetChanged();
    }

    // 🔄 Update list dari Firebase / SearchFragment
    public void updateRecipeList(List<RecipeSearch> newRecipeList) {
        recipeListFull.clear();
        recipeListFull.addAll(newRecipeList);
        recipeList.clear();
        recipeList.addAll(newRecipeList);
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivRecipeImage;
        private final TextView tvRecipeName;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeImage = itemView.findViewById(R.id.RecipeImage);
            tvRecipeName = itemView.findViewById(R.id.TitleRecipe);
        }

        public void bind(RecipeSearch recipe) {
            // 🔹 Nama resep
            tvRecipeName.setText(recipe.getDisplayName() != null ? recipe.getDisplayName() : "Tanpa Judul");

            // 🔹 Gambar resep (pakai Glide)
            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(recipe.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(ivRecipeImage);
            } else {
                ivRecipeImage.setImageResource(R.drawable.placeholder_image);
            }

            // 🔹 Klik item → buka RecipeActivity
            itemView.setOnClickListener(v -> {
                RecipeActivity.open(
                        itemView.getContext(),
                        recipe.getId(),
                        recipe.getDisplayName(),
                        recipe.getImageUrl()
                );
            });
        }
    }
}
