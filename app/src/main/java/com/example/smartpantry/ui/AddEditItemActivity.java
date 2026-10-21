package com.example.smartpantry.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantry.R;
import com.example.smartpantry.data.AppDatabase;
import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.logic.DateUtils;

import java.util.Calendar;

/** Create a new pantry item, or edit / delete an existing one (when EXTRA_ITEM_ID is passed). */
public class AddEditItemActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";

    private EditText etName, etQty, etExpiry;
    private Spinner spUnit;
    private Button btnDelete;
    private long itemId = -1;
    private PantryItem existing;
    private String[] units;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_item);

        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        etExpiry = findViewById(R.id.etExpiry);
        spUnit = findViewById(R.id.spUnit);
        btnDelete = findViewById(R.id.btnDelete);
        Button btnSave = findViewById(R.id.btnSave);

        units = getResources().getStringArray(R.array.units);
        spUnit.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, units));

        etExpiry.setOnClickListener(v -> pickDate());
        etExpiry.setOnLongClickListener(v -> { etExpiry.setText(""); return true; });
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());

        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId == -1) {
            setTitle("Add Ingredient");
        } else {
            setTitle("Edit Ingredient");
            btnDelete.setVisibility(android.view.View.VISIBLE);
            loadExisting();
        }
    }

    private void loadExisting() {
        AppDatabase.EXECUTOR.execute(() -> {
            existing = AppDatabase.get(this).pantryDao().getById(itemId);
            runOnUiThread(() -> {
                if (existing == null) { finish(); return; }
                etName.setText(existing.name);
                etQty.setText(PantryAdapter.trim(existing.quantity));
                etExpiry.setText(existing.expiryDate == null ? "" : existing.expiryDate);
                for (int i = 0; i < units.length; i++) {
                    if (units[i].equals(existing.unit)) spUnit.setSelection(i);
                }
            });
        });
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> etExpiry.setText(DateUtils.format(y, m, d)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Validates the form, then inserts (Create) or updates (Update) on the DB thread. */
    private void save() {
        String name = etName.getText().toString().trim();
        String qtyText = etQty.getText().toString().trim();
        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            valid = false;
        }
        double qty = 0;
        if (TextUtils.isEmpty(qtyText)) {
            etQty.setError("Quantity is required");
            valid = false;
        } else {
            try {
                qty = Double.parseDouble(qtyText);
                if (qty <= 0) { etQty.setError("Must be greater than 0"); valid = false; }
            } catch (NumberFormatException e) {
                etQty.setError("Enter a valid number");
                valid = false;
            }
        }
        if (!valid) return;

        String unit = spUnit.getSelectedItem().toString();
        String expiry = etExpiry.getText().toString().trim();
        if (expiry.isEmpty()) expiry = null;

        final double finalQty = qty;
        final String finalExpiry = expiry;
        AppDatabase.EXECUTOR.execute(() -> {
            if (itemId == -1) {
                AppDatabase.get(this).pantryDao().insert(new PantryItem(name, finalQty, unit, finalExpiry));
            } else {
                existing.name = name;
                existing.quantity = finalQty;
                existing.unit = unit;
                existing.expiryDate = finalExpiry;
                AppDatabase.get(this).pantryDao().update(existing);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete this ingredient?")
                .setPositiveButton("Delete", (d, w) -> AppDatabase.EXECUTOR.execute(() -> {
                    AppDatabase.get(this).pantryDao().delete(existing);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}
