package com.example.smartpantry.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.Recipe;
import com.example.smartpantry.data.RecipeIngredient;
import com.example.smartpantry.logic.RecipeMatcher;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs the strict-matching logic against the pantry. The main list contains ONLY recipes
 * with zero missing ingredients; the optional "Almost there" list (recipes missing exactly
 * one ingredient) is a separate view and is never mixed into the strict list.
 */
public class SuggestedRecipesActivity extends AppCompatActivity {

    private final List<RecipeAdapter.Row> strictRows = new ArrayList<>();
    private final List<RecipeAdapter.Row> almostRows = new ArrayList<>();
    private boolean showingAlmost = false;

    private RecipeAdapter adapter;
    private TextView tvEmpty, tvNote;
    private Button btnStrict, btnAlmost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);
        setTitle("Suggested Recipes");

        tvEmpty = findViewById(R.id.tvEmpty);
        tvNote = findViewById(R.id.tvSectionNote);
        btnStrict = findViewById(R.id.btnStrict);
        btnAlmost = findViewById(R.id.btnAlmost);

        adapter = new RecipeAdapter(recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(i);
        });
        RecyclerView rv = findViewById(R.id.rvRecipes);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnStrict.setOnClickListener(v -> { showingAlmost = false; render(); });
        btnAlmost.setOnClickListener(v -> { showingAlmost = true; render(); });

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_recipes);
    }

    @Override
    protected void onResume() {
        super.onResume();
        computeMatches();
    }

    /** Loads pantry + recipes on the DB thread and sorts every recipe into a list. */
    private void computeMatches() {
        boolean excludeExpired = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE)
                .getBoolean(SettingsActivity.KEY_EXCLUDE_EXPIRED, true);

        AppDatabase.EXECUTOR.execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            List<PantryItem> pantry = db.pantryDao().getAll();
            List<Recipe> recipes = db.recipeDao().getAll();

            Map<Long, List<RecipeIngredient>> byRecipe = new HashMap<>();
            for (RecipeIngredient ing : db.recipeDao().getAllIngredients()) {
                List<RecipeIngredient> list = byRecipe.get(ing.recipeId);
                if (list == null) { list = new ArrayList<>(); byRecipe.put(ing.recipeId, list); }
                list.add(ing);
            }

            Map<String, Double> stock = RecipeMatcher.buildStock(pantry, excludeExpired);
            List<RecipeAdapter.Row> strict = new ArrayList<>();
            List<RecipeAdapter.Row> almost = new ArrayList<>();

            for (Recipe r : recipes) {
                List<RecipeIngredient> needed = byRecipe.get(r.id);
                if (needed == null || needed.isEmpty()) continue;
                int missing = RecipeMatcher.countMissing(needed, stock);
                if (missing == 0) {
                    strict.add(new RecipeAdapter.Row(r, "You have all " + needed.size() + " ingredients"));
                } else if (missing == 1) {
                    almost.add(new RecipeAdapter.Row(r, "Missing 1 of " + needed.size() + " ingredients"));
                }
            }

            runOnUiThread(() -> {
                strictRows.clear(); strictRows.addAll(strict);
                almostRows.clear(); almostRows.addAll(almost);
                render();
            });
        });
    }

    private void render() {
        List<RecipeAdapter.Row> rows = showingAlmost ? almostRows : strictRows;
        adapter.setRows(rows);
        btnStrict.setEnabled(showingAlmost);
        btnAlmost.setEnabled(!showingAlmost);
        tvNote.setText(showingAlmost
                ? "Not suggestions yet: each of these needs one more ingredient."
                : "Every ingredient for these is already in your pantry.");

        if (rows.isEmpty()) {
            tvEmpty.setText(showingAlmost
                    ? "No recipes are one ingredient away."
                    : "No recipes match your pantry yet - add more ingredients");
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}
