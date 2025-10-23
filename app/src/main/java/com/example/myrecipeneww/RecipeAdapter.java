package com.example.myrecipeneww;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.Holder> {

    private List<Recipe> data;
    private final Context ctx;
    private final FirebaseHelper firebaseHelper;

    public RecipeAdapter(List<Recipe> data, Context ctx, FirebaseHelper firebaseHelper) {
        this.data = data;
        this.ctx = ctx;
        this.firebaseHelper = firebaseHelper;
    }

    public void setData(List<Recipe> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.item_my_recipe, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Recipe r = data.get(position);

        holder.tvTitle.setText(r.getTitle() != null ? r.getTitle() : "Untitled Recipe");
        holder.tvDescription.setText(r.getRingkasan() != null ? r.getRingkasan() : "-");

        // Gambar
        if (r.getImageUrl() != null && !r.getImageUrl().isEmpty()) {
            Glide.with(ctx).load(r.getImageUrl()).into(holder.ivPreview);
        } else {
            holder.ivPreview.setImageResource(R.drawable.placeholder_image);
        }

        // Tombol Cook
        holder.btnCook.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            firebaseHelper.startCooking(ctx, data.get(pos).getId());
        });

        // Tombol Edit
        holder.btnEdit.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            firebaseHelper.startEditRecipe(ctx, data.get(pos));
        });

        // Tombol Delete
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Recipe recipeToDelete = data.get(pos);

            firebaseHelper.deleteRecipeForCurrentUser(recipeToDelete.getId(), new FirebaseHelper.SimpleCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ctx, "Resep dihapus", Toast.LENGTH_SHORT).show();
                    data.remove(pos);
                    notifyItemRemoved(pos);
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(ctx, "Gagal menghapus resep: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Icon Favorite
        holder.icFav.setVisibility(View.VISIBLE);
        holder.icFav.setImageResource(r.isFavorite() ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite);

        // Klik toggle favorite
        holder.icFav.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Recipe current = data.get(pos);
            boolean newStatus = !current.isFavorite();

            firebaseHelper.updateFavoriteStatus(current.getId(), newStatus, new FirebaseHelper.SimpleCallback() {
                @Override
                public void onSuccess() {
                    current.setFavorite(newStatus);
                    holder.icFav.setImageResource(newStatus
                            ? R.drawable.ic_favorite_filled
                            : R.drawable.ic_favorite
                    );

                    Toast.makeText(ctx,
                            newStatus ? "Ditambahkan ke favorit" : "Dihapus dari favorit",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailed(Exception e) {
                    Toast.makeText(ctx, "Gagal update favorit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView ivPreview, icFav;
        TextView tvTitle, tvDescription;
        Button btnCook, btnEdit, btnDelete;

        Holder(@NonNull View itemView) {
            super(itemView);
            ivPreview = itemView.findViewById(R.id.imgRecipe);
            tvTitle = itemView.findViewById(R.id.tvRecipeTitle);
            tvDescription = itemView.findViewById(R.id.tvRecipeDescription);
            btnCook = itemView.findViewById(R.id.btnCook);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            icFav = itemView.findViewById(R.id.icFav);
        }
    }
}
