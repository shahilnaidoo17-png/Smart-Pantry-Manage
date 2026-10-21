package com.example.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.Holder> {

    /** A recipe plus the small line of text shown under its name. */
    public static class Row {
        final Recipe recipe;
        final String subtitle;
        public Row(Recipe recipe, String subtitle) { this.recipe = recipe; this.subtitle = subtitle; }
    }

    public interface Listener { void onClick(Recipe recipe); }

    private final List<Row> rows = new ArrayList<>();
    private final Listener listener;

    public RecipeAdapter(Listener listener) { this.listener = listener; }

    public void setRows(List<Row> newRows) {
        rows.clear();
        rows.addAll(newRows);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Row row = rows.get(position);
        h.name.setText(row.recipe.name);
        h.sub.setText(row.subtitle);
        h.itemView.setOnClickListener(v -> listener.onClick(row.recipe));
    }

    @Override public int getItemCount() { return rows.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name, sub;
        Holder(View v) {
            super(v);
            name = v.findViewById(R.id.tvRecipeName);
            sub = v.findViewById(R.id.tvRecipeSub);
        }
    }
}
