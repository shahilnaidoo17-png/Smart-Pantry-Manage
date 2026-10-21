package com.example.smartpantry.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/** One ingredient the user currently has at home. */
@Entity(tableName = "pantry_items")
public class PantryItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    public double quantity;
    public String unit;
    /** ISO date "yyyy-MM-dd", or null when the user did not enter one. */
    public String expiryDate;

    public PantryItem() { }

    @Ignore
    public PantryItem(String name, double quantity, String unit, String expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }
}
