package com.example.smartpantry.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room (SQLite) database singleton. All access happens on EXECUTOR, a single
 * background thread, so the UI thread is never blocked and seeding always finishes
 * before the first query that is queued after it.
 */
@Database(entities = {PantryItem.class, Recipe.class, RecipeIngredient.class},
        version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static volatile AppDatabase instance;

    public abstract PantryDao pantryDao();
    public abstract RecipeDao recipeDao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "smart_pantry.db").build();
                }
            }
        }
        return instance;
    }
}
