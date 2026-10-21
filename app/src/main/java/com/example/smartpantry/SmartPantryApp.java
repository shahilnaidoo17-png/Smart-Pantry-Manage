package com.example.smartpantry;

import android.app.Application;
import android.util.Log;

import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.RecipeSeeder;

public class SmartPantryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppDatabase.EXECUTOR.execute(() -> {
            AppDatabase db = AppDatabase.get(this);
            RecipeSeeder.seedIfEmpty(db);

            int recipeCount = db.recipeDao().count();
            int ingredientCount = db.recipeDao().getAllIngredients().size();

            Log.d("SmartPantryCheck",
                    "Recipes loaded: " + recipeCount
                    + ", recipe ingredients loaded: " + ingredientCount);
        });
    }
}