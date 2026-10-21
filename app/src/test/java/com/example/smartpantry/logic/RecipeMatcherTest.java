package com.example.smartpantry.logic;

import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.RecipeIngredient;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RecipeMatcherTest {

    @Test
    public void normalizesEggsToEgg() {
        assertEquals("egg", RecipeMatcher.normalizeName("Eggs"));
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
    }

    @Test
    public void matchesWhenAllIngredientsAndQuantitiesAreAvailable() {
        List<RecipeIngredient> needed = Arrays.asList(
                new RecipeIngredient(1, "Eggs", 2, "pcs"),
                new RecipeIngredient(1, "Cheese", 50, "g")
        );

        Map<String, Double> stock = new HashMap<>();
        stock.put("egg|count", 2.0);
        stock.put("cheese|mass", 50.0);

        assertTrue(RecipeMatcher.isStrictMatch(needed, stock));
        assertEquals(0, RecipeMatcher.countMissing(needed, stock));
    }

    @Test
    public void rejectsInsufficientQuantity() {
        List<RecipeIngredient> needed = Collections.singletonList(
                new RecipeIngredient(1, "Egg", 3, "pcs")
        );

        Map<String, Double> stock = new HashMap<>();
        stock.put("egg|count", 2.0);

        assertFalse(RecipeMatcher.isStrictMatch(needed, stock));
        assertEquals(1, RecipeMatcher.countMissing(needed, stock));
    }

    @Test
    public void rejectsMissingIngredient() {
        List<RecipeIngredient> needed = Collections.singletonList(
                new RecipeIngredient(1, "Butter", 10, "g")
        );

        assertFalse(RecipeMatcher.isStrictMatch(
                needed, Collections.<String, Double>emptyMap()));
    }

    @Test
    public void combinesDuplicatePantryEntries() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Egg", 2, "pcs", null),
                new PantryItem("Eggs", 3, "pcs", null)
        );

        Map<String, Double> stock = RecipeMatcher.buildStock(pantry, false);

        assertEquals(5.0, stock.get("egg|count"), 0.000001);
    }

    @Test
    public void excludesExpiredItemsWhenRequested() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Butter", 10, "g", "2020-01-01")
        );

        Map<String, Double> excluded = RecipeMatcher.buildStock(pantry, true);
        Map<String, Double> included = RecipeMatcher.buildStock(pantry, false);

        assertFalse(excluded.containsKey("butter|mass"));
        assertEquals(10.0, included.get("butter|mass"), 0.000001);
    }

    @Test
    public void rejectsEmptyRecipe() {
        assertFalse(RecipeMatcher.isStrictMatch(
                Collections.<RecipeIngredient>emptyList(),
                Collections.<String, Double>emptyMap()));
    }

    // Test 8: Handles capitalisation and extra spaces.
    @Test
    public void normalizesCapitalisationAndExtraSpaces() {
        assertEquals("egg", RecipeMatcher.normalizeName("  EGGS  "));
        assertEquals("tomato", RecipeMatcher.normalizeName("Tomatoes"));
    }

    // Test 9: Converts kilograms to grams for matching.
    @Test
    public void matchesKilogramsAgainstGrams() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Flour", 1, "kg", null)
        );

        Map<String, Double> stock = RecipeMatcher.buildStock(pantry, false);

        List<RecipeIngredient> needed = Collections.singletonList(
                new RecipeIngredient(1, "Flour", 500, "g")
        );

        assertTrue(RecipeMatcher.isStrictMatch(needed, stock));
    }

    // Test 10: Does not match mass against volume.
    @Test
    public void rejectsIncompatibleUnits() {
        List<RecipeIngredient> needed = Collections.singletonList(
                new RecipeIngredient(1, "Water", 1, "l")
        );

        Map<String, Double> stock = new HashMap<>();
        stock.put("water|mass", 1000.0);

        assertFalse(RecipeMatcher.isStrictMatch(needed, stock));
    }

    // Test 11: Combines duplicate entries in compatible units.
    @Test
    public void combinesDuplicateEntriesAcrossCompatibleUnits() {
        List<PantryItem> pantry = Arrays.asList(
                new PantryItem("Flour", 0.5, "kg", null),
                new PantryItem("Flour", 500, "g", null)
        );

        Map<String, Double> stock = RecipeMatcher.buildStock(pantry, false);

        assertEquals(1000.0, stock.get("flour|mass"), 0.000001);
    }

    // Test 12: Keeps items whose expiry date was not entered.
    @Test
    public void includesItemsWithNoExpiryDate() {
        List<PantryItem> pantry = Collections.singletonList(
                new PantryItem("Milk", 1, "l", null)
        );

        Map<String, Double> stock = RecipeMatcher.buildStock(pantry, true);

        assertEquals(1000.0, stock.get("milk|volume"), 0.000001);
    }

    // Test 13: Rejects a quantity that is slightly too small.
    @Test
    public void rejectsQuantityJustBelowRequirement() {
        List<RecipeIngredient> needed = Collections.singletonList(
                new RecipeIngredient(1, "Cheese", 50, "g")
        );

        Map<String, Double> stock = new HashMap<>();
        stock.put("cheese|mass", 49.9);

        assertFalse(RecipeMatcher.isStrictMatch(needed, stock));
    }
}
