package com.example.smartpantry.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;

/** Two persisted preferences, stored in SharedPreferences. */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS = "smart_pantry_prefs";
    public static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    public static final String KEY_EXCLUDE_EXPIRED = "exclude_expired";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");

        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SwitchMaterial swExpiry = findViewById(R.id.swExpiry);
        SwitchMaterial swExclude = findViewById(R.id.swExcludeExpired);

        swExpiry.setChecked(prefs.getBoolean(KEY_EXPIRY_ALERTS, true));
        swExclude.setChecked(prefs.getBoolean(KEY_EXCLUDE_EXPIRED, true));
        swExpiry.setOnCheckedChangeListener((b, on) -> prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, on).apply());
        swExclude.setOnCheckedChangeListener((b, on) -> prefs.edit().putBoolean(KEY_EXCLUDE_EXPIRED, on).apply());

        NavHelper.setup(this, (BottomNavigationView) findViewById(R.id.bottomNav), R.id.nav_settings);
    }
}
