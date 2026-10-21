package com.example.smartpantry.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.Recipe;
import com.example.smartpantry.data.RecipeIngredient;

import java.util.List;

/** Shows the full ingredient list and method for the recipe id received through the Intent. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        long id = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvIngredients = findViewById(R.id.tvIngredients);
        TextView tvSteps = findViewById(R.id.tvSteps);

        AppDatabase.EXECUTOR.execute(() -> {
            Recipe recipe = AppDatabase.get(this).recipeDao().getById(id);
            List<RecipeIngredient> ings = AppDatabase.get(this).recipeDao().getIngredients(id);
            runOnUiThread(() -> {
                if (recipe == null) { finish(); return; }
                setTitle(recipe.name);
                tvTitle.setText(recipe.name);
                StringBuilder sb = new StringBuilder();
                for (RecipeIngredient i : ings) {
                    sb.append("\u2022 ").append(PantryAdapter.trim(i.quantity)).append(' ')
                      .append(i.unit).append(' ').append(i.name).append('\n');
                }
                tvIngredients.setText(sb.toString().trim());
                tvSteps.setText(recipe.steps);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
