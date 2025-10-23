package com.example.myrecipeneww;

import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class RecipeAdapterFavorite extends RecyclerView.Adapter<RecipeAdapterFavorite.RecipeViewHolder> {

    private Context context;
    private ArrayList<RecipeFavorite> recipeList;
    private FirebaseHelper firebaseHelper;

    public RecipeAdapterFavorite(Context context, ArrayList<RecipeFavorite> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
        this.firebaseHelper = new FirebaseHelper();
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
        RecipeFavorite recipe = recipeList.get(position);

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(context).load(recipe.getImageUrl()).into(holder.recipeImage);
        } else {
            holder.recipeImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.recipeTitle.setText(recipe.getTitle());
        holder.recipeDescription.setText(recipe.getDescription());
        holder.heartIcon.setImageResource(recipe.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);

        // Cook button
        holder.btnCook.setOnClickListener(v ->
                Toast.makeText(context, "Memasak: " + recipe.getTitle(), Toast.LENGTH_SHORT).show()
        );

        holder.heartIcon.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            RecipeFavorite current = recipeList.get(pos);
            boolean newStatus = !current.isFavorite();

            // Hanya update isFavorite di Firebase
            firebaseHelper.updateFavoriteStatus(current.getId(), newStatus, new FirebaseHelper.SimpleCallback() {
                @Override
                public void onSuccess() {
                    current.setFavorite(newStatus);

                    if (!newStatus) {
                        // Remove dari FavoriteFragment list saja
                        recipeList.remove(pos);
                        notifyItemRemoved(pos);
                    } else {
                        notifyItemChanged(pos);
                    }

                    Toast.makeText(context,
                            newStatus ? "Ditambahkan ke favorit" : "Dihapus dari favorit",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(context, "Gagal update favorit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage, heartIcon;
        TextView recipeTitle, recipeDescription;
        Button btnCook;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.imgRecipe);
            recipeTitle = itemView.findViewById(R.id.tvRecipeTitle);
            recipeDescription = itemView.findViewById(R.id.tvRecipeDescription);
            btnCook = itemView.findViewById(R.id.btnCook);
            heartIcon = itemView.findViewById(R.id.icFav);
        }
    }
}
