package com.example.smartpantry;

import android.app.Application;

import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.RecipeSeeder;

public class SmartPantryApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Queued first on the single DB thread, so every later query sees the seeded recipes.
        AppDatabase.EXECUTOR.execute(() -> RecipeSeeder.seedIfEmpty(AppDatabase.get(this)));
    }
}
