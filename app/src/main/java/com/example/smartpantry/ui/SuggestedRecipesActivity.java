package com.example.smartpantry.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
        ).getBoolean(SettingsActivity.KEY_EXCLUDE_EXPIRED, true);

        AppDatabase.EXECUTOR.execute(() -> {
            AppDatabase db = AppDatabase.get(this);

            List<PantryItem> pantry = db.pantryDao().getAll();
            List<Recipe> recipes = db.recipeDao().getAll();
            List<RecipeIngredient> allIngredients =
                    db.recipeDao().getAllIngredients();

            Map<Long, List<RecipeIngredient>> byRecipe = new HashMap<>();

            for (RecipeIngredient ing : allIngredients) {
                List<RecipeIngredient> list = byRecipe.get(ing.recipeId);

                if (list == null) {
                    list = new ArrayList<>();
                    byRecipe.put(ing.recipeId, list);
                }

                list.add(ing);
            }

            Map<String, Double> stock =
                    RecipeMatcher.buildStock(pantry, excludeExpired);

            List<RecipeAdapter.Row> strict = new ArrayList<>();
            List<RecipeAdapter.Row> almost = new ArrayList<>();

            for (Recipe recipe : recipes) {
                List<RecipeIngredient> needed = byRecipe.get(recipe.id);

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

            StringBuilder debug = new StringBuilder();

            debug.append("PANTRY DATA\n");

            for (PantryItem item : pantry) {
                debug.append(item.name)
                        .append(" | quantity=")
                        .append(item.quantity)
                        .append(" | unit=")
                        .append(item.unit)
                        .append(" | expiry=")
                        .append(item.expiryDate)
                        .append("\n");
            }

            debug.append("\nNORMALISED STOCK\n");

            for (Map.Entry<String, Double> entry : stock.entrySet()) {
                debug.append(entry.getKey())
                        .append(" = ")
                        .append(entry.getValue())
                        .append("\n");
            }

            for (Recipe recipe : recipes) {
                if ("Cheese Omelette".equalsIgnoreCase(recipe.name)) {
                    List<RecipeIngredient> needed = byRecipe.get(recipe.id);

                    debug.append("\nCHEESE OMELETTE DIAGNOSIS\n");

                    if (needed != null && !needed.isEmpty()) {
                        debug.append(
                                RecipeMatcher.diagnose(needed, stock)
                        );

                        debug.append("\nMissing count: ")
                                .append(
                                        RecipeMatcher.countMissing(needed, stock)
                                );
                    } else {
                        debug.append("Recipe ingredients were not found.");
                    }

                    break;
                }
            }

            String debugMessage = debug.toString();
            int ingredientCount = allIngredients.size();

            runOnUiThread(() -> {
                strictRows.clear();
                strictRows.addAll(strict);

                almostRows.clear();
                almostRows.addAll(almost);

                render();

                // Show detailed diagnostics after rendering the recipe list.
                tvNote.setText(debugMessage);

                Toast.makeText(
                        SuggestedRecipesActivity.this,
                        "Recipes: " + recipes.size()
                                + " | Ingredients: " + ingredientCount
                                + " | Pantry: " + pantry.size(),
                        Toast.LENGTH_LONG
                ).show();
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

