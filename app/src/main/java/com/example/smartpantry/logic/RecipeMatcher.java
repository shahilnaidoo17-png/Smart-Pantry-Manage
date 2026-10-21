package com.example.smartpantry.logic;

import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.RecipeIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The strict-matching rule (assignment section 2.3).
 *
 * A recipe is a strict match only if EVERY ingredient is in the pantry in at least the
 * required quantity. A missing ingredient, or too little of one, means the recipe is excluded.
 *
 * Robustness: names are normalised (case, whitespace, simple plurals) and quantities are
 * converted to a common base unit, so "Tomatoes" matches "tomato" and 1 kg covers 500 g.
 * Units of different kinds (e.g. grams vs pieces) are never assumed to be equal.
 */
public class RecipeMatcher {

    /** Lower-cases, trims, and singularises the last word: "Tomatoes " -> "tomato". */
    public static String normalizeName(String raw) {
        if (raw == null) return "";
        String s = raw.toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
        int space = s.lastIndexOf(' ');
        String head = space >= 0 ? s.substring(0, space + 1) : "";
        String last = space >= 0 ? s.substring(space + 1) : s;
        return head + singular(last);
    }

    private static String singular(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";       // berries -> berry
        if (w.endsWith("oes") || w.endsWith("ches") || w.endsWith("shes")
                || w.endsWith("xes")) return w.substring(0, w.length() - 2);       // tomatoes -> tomato
        if (w.endsWith("ss") || w.endsWith("us")) return w;                        // hummus, molasses
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);                // eggs -> egg
        return w;
    }

    /** Key combining the normalised name and the unit dimension. */
    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + UnitConverter.dimension(unit);
    }

    /** Total pantry stock per ingredient (duplicate pantry rows are summed), in base units. */
    public static Map<String, Double> buildStock(List<PantryItem> pantry, boolean excludeExpired) {
        Map<String, Double> stock = new HashMap<>();
        for (PantryItem item : pantry) {
            if (excludeExpired) {
                Integer days = DateUtils.daysUntil(item.expiryDate);
                if (days != null && days < 0) continue;
            }
            String k = key(item.name, item.unit);
            double amount = UnitConverter.toBase(item.quantity, item.unit);
            Double current = stock.get(k);
            stock.put(k, (current == null ? 0 : current) + amount);
        }
        return stock;
    }

    /** How many of the recipe's ingredients are missing or insufficient. */
    public static int countMissing(List<RecipeIngredient> needed, Map<String, Double> stock) {
        int missing = 0;
        for (RecipeIngredient ing : needed) {
            Double have = stock.get(key(ing.name, ing.unit));
            double required = UnitConverter.toBase(ing.quantity, ing.unit);
            if (have == null || have + 1e-6 < required) missing++;
        }
        return missing;
    }

    /** True only when nothing is missing. A recipe with no ingredients is never suggested. */
    public static boolean isStrictMatch(List<RecipeIngredient> needed, Map<String, Double> stock) {
        return !needed.isEmpty() && countMissing(needed, stock) == 0;
    }
}
