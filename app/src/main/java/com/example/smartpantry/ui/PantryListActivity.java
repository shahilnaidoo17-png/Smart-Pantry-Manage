package com.example.smartpantry.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.logic.DateUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/** Home screen: lists every pantry item (Read) and links to add / edit / delete. */
public class PantryListActivity extends AppCompatActivity {

    private PantryAdapter adapter;
    private TextView tvEmpty, tvBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);
        setTitle("My Pantry");

        tvEmpty = findViewById(R.id.tvEmpty);
        tvBanner = findViewById(R.id.tvBanner);

        adapter = new PantryAdapter(new PantryAdapter.Listener() {
            @Override public void onClick(PantryItem item) {
                // Explicit Intent carrying the row id to the edit screen
                Intent i = new Intent(PantryListActivity.this, AddEditItemActivity.class);
                i.putExtra(AddEditItemActivity.EXTRA_ITEM_ID, item.id);
                startActivity(i);
            }
            @Override public void onLongClick(PantryItem item) { confirmDelete(item); }
        });
        RecyclerView rv = findViewById(R.id.rvPantry);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddEditItemActivity.class)));

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_pantry);
    }

    /** Reload every time the screen becomes visible, so edits made elsewhere show up. */
    @Override
    protected void onResume() {
        super.onResume();
        loadItems();
    }

    private void loadItems() {
        AppDatabase.EXECUTOR.execute(() -> {
            List<PantryItem> items = AppDatabase.get(this).pantryDao().getAll();
            runOnUiThread(() -> {
                adapter.setItems(items);
                tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                updateBanner(items);
            });
        });
    }

    private void updateBanner(List<PantryItem> items) {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE);
        int count = 0;
        for (PantryItem item : items) {
            Integer days = DateUtils.daysUntil(item.expiryDate);
            if (days != null && days <= 3) count++;
        }
        if (prefs.getBoolean(SettingsActivity.KEY_EXPIRY_ALERTS, true) && count > 0) {
            tvBanner.setText(count + " item(s) expired or expiring within 3 days");
            tvBanner.setVisibility(View.VISIBLE);
        } else {
            tvBanner.setVisibility(View.GONE);
        }
    }

    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete " + item.name + "?")
                .setPositiveButton("Delete", (d, w) -> AppDatabase.EXECUTOR.execute(() -> {
                    AppDatabase.get(this).pantryDao().delete(item);
                    runOnUiThread(() -> Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show());
                    loadItems();
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
