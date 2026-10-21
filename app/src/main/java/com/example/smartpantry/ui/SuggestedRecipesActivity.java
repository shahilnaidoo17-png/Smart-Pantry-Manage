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

public class SuggestedRecipesActivity extends AppCompatActivity {

    private final List<RecipeAdapter.Row> strictRows = new ArrayList<>();
    private final List<RecipeAdapter.Row> almostRows = new ArrayList<>();

    private boolean showingAlmost = false;

    private RecipeAdapter adapter;
    private TextView tvEmpty;
    private TextView tvNote;
    private Button btnStrict;
    private Button btnAlmost;

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
            Intent intent = new Intent(
                    SuggestedRecipesActivity.this,
                    RecipeDetailActivity.class
            );

            intent.putExtra(
                    RecipeDetailActivity.EXTRA_RECIPE_ID,
                    recipe.id
            );

            startActivity(intent);
        });

        RecyclerView recyclerView = findViewById(R.id.rvRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnStrict.setOnClickListener(v -> {
            showingAlmost = false;
            render();
        });

        btnAlmost.setOnClickListener(v -> {
            showingAlmost = true;
            render();
        });

        NavHelper.setup(
                this,
                (BottomNavigationView) findViewById(R.id.bottomNav),
                R.id.nav_recipes
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        computeMatches();
    }

    private void computeMatches() {
        boolean excludeExpired = getSharedPreferences(
                SettingsActivity.PREFS,
                Context.MODE_PRIVATE
        ).getBoolean(
                SettingsActivity.KEY_EXCLUDE_EXPIRED,
                true
        );

        AppDatabase.EXECUTOR.execute(() -> {
            AppDatabase db = AppDatabase.get(getApplicationContext());

            List<PantryItem> pantry = db.pantryDao().getAll();
            List<Recipe> recipes = db.recipeDao().getAll();

            List<RecipeIngredient> allIngredients =
                    db.recipeDao().getAllIngredients();

            Map<Long, List<RecipeIngredient>> ingredientsByRecipe =
                    new HashMap<>();

            for (RecipeIngredient ingredient : allIngredients) {
                List<RecipeIngredient> ingredients =
                        ingredientsByRecipe.get(ingredient.recipeId);

                if (ingredients == null) {
                    ingredients = new ArrayList<>();
                    ingredientsByRecipe.put(
                            ingredient.recipeId,
                            ingredients
                    );
                }

                ingredients.add(ingredient);
            }

            Map<String, Double> stock =
                    RecipeMatcher.buildStock(pantry, excludeExpired);

            List<RecipeAdapter.Row> strict = new ArrayList<>();
            List<RecipeAdapter.Row> almost = new ArrayList<>();

            for (Recipe recipe : recipes) {
                List<RecipeIngredient> needed =
                        ingredientsByRecipe.get(recipe.id);

                if (needed == null || needed.isEmpty()) {
                    continue;
                }

                int missing = RecipeMatcher.countMissing(needed, stock);

                if (missing == 0) {
                    strict.add(new RecipeAdapter.Row(
                            recipe,
                            "You have all " + needed.size() + " ingredients"
                    ));
                } else if (missing == 1) {
                    almost.add(new RecipeAdapter.Row(
                            recipe,
                            "Missing 1 of " + needed.size() + " ingredients"
                    ));
                }
            }

            runOnUiThread(() -> {
                strictRows.clear();
                strictRows.addAll(strict);

                almostRows.clear();
                almostRows.addAll(almost);

                render();
            });
        });
    }

    private void render() {
        List<RecipeAdapter.Row> rows =
                showingAlmost ? almostRows : strictRows;

        adapter.setRows(rows);

        btnStrict.setEnabled(showingAlmost);
        btnAlmost.setEnabled(!showingAlmost);

        tvNote.setText(showingAlmost
                ? "These recipes each need one more ingredient."
                : "You have all the ingredients needed for these recipes.");

        if (rows.isEmpty()) {
            tvEmpty.setText(showingAlmost
                    ? "No recipes are one ingredient away. Add more pantry ingredients to discover recipes."
                    : "No recipes match your pantry yet. Add more ingredients to discover recipes.");

            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }
}

