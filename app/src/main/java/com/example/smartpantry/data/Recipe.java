package com.example.smartpantry.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "recipes")
public class Recipe {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String name;
    /** Preparation steps, one per line. */
    public String steps;

    public Recipe() { }

    @Ignore
    public Recipe(String name, String steps) {
        this.name = name;
        this.steps = steps;
    }
}
