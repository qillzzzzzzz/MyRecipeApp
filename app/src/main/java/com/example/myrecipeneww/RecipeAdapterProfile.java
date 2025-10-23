package com.example.myrecipeneww;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class RecipeAdapterProfile extends RecyclerView.Adapter<RecipeAdapterProfile.RecipeViewHolder> {

    private Context context;
    private ArrayList<RecipeFavorite> favoriteList;

    public RecipeAdapterProfile(Context context, ArrayList<RecipeFavorite> favoriteList) {
        this.context = context;
        this.favoriteList = favoriteList;
    }

    public void updateData(ArrayList<RecipeFavorite> newList) {
        this.favoriteList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        RecipeFavorite recipe = favoriteList.get(position);

        holder.recipeName.setText(recipe.getTitle());

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            // TODO: buka detail resep
        });
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.imgRecipe);
            recipeName = itemView.findViewById(R.id.tvRecipeTitle);
        }
    }
}
