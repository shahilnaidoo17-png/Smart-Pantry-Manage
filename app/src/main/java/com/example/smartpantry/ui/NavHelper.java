package com.example.smartpantry.ui;

import android.app.Activity;
import android.content.Intent;

import com.example.smartpantry.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Wires the bottom navigation bar; each tab is opened with an explicit Intent. */
public class NavHelper {
    public static void setup(Activity activity, BottomNavigationView nav, int currentId) {
        nav.setSelectedItemId(currentId);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentId) return true;
            Class<?> target;
            if (id == R.id.nav_pantry) target = PantryListActivity.class;
            else if (id == R.id.nav_recipes) target = SuggestedRecipesActivity.class;
            else target = SettingsActivity.class;
            Intent intent = new Intent(activity, target);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.startActivity(intent);
            return true;
        });
    }
}
