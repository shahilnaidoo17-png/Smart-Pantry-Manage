package com.example.smartpantry.logic;

import com.example.smartpantry.data.PantryItem;
import com.example.smartpantry.data.RecipeIngredient;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RecipeMatcher {

    public static String normalizeName(String raw) {
        if (raw == null) {
            return "";
        }

        String name = raw.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");

        // Normalise common irregular and common plural ingredient names.
        if (name.equals("eggs")) return "egg";
        if (name.equals("tomatoes")) return "tomato";
        if (name.equals("potatoes")) return "potato";
        if (name.equals("loaves")) return "loaf";
        if (name.equals("leaves")) return "leaf";

        int space = name.lastIndexOf(' ');
        String prefix = space >= 0 ? name.substring(0, space + 1) : "";
        String lastWord = space >= 0
                ? name.substring(space + 1)
                : name;

        return prefix + singular(lastWord);
    }

    private static String singular(String word) {
        if (word.length() <= 3) {
            return word;
        }

        if (word.endsWith("ies")) {
            return word.substring(0, word.length() - 3) + "y";
        }

        if (word.endsWith("oes")
                || word.endsWith("ches")
                || word.endsWith("shes")
                || word.endsWith("xes")) {
            return word.substring(0, word.length() - 2);
        }

        if (word.endsWith("ss") || word.endsWith("us")) {
            return word;
        }

        if (word.endsWith("s")) {
            return word.substring(0, word.length() - 1);
        }

        return word;
    }

    private static String key(String name, String unit) {
        return normalizeName(name) + "|" + UnitConverter.dimension(unit);
    }

    public static Map<String, Double> buildStock(
            List<PantryItem> pantry,
            boolean excludeExpired) {

        Map<String, Double> stock = new HashMap<>();

        for (PantryItem item : pantry) {
            if (item == null || item.name == null || item.unit == null) {
                continue;
            }

            if (excludeExpired) {
                Integer days = DateUtils.daysUntil(item.expiryDate);

                if (days != null && days < 0) {
                    continue;
                }
            }

            String stockKey = key(item.name, item.unit);
            double amount = UnitConverter.toBase(
                    item.quantity, item.unit);

            stock.put(
                    stockKey,
                    stock.getOrDefault(stockKey, 0.0) + amount
            );
        }

        return stock;
    }

    public static int countMissing(
            List<RecipeIngredient> needed,
            Map<String, Double> stock) {

        int missing = 0;

        for (RecipeIngredient ingredient : needed) {
            if (ingredient == null
                    || ingredient.name == null
                    || ingredient.unit == null) {
                missing++;
                continue;
            }

            String ingredientKey = key(
                    ingredient.name, ingredient.unit);

            Double available = stock.get(ingredientKey);

            double required = UnitConverter.toBase(
                    ingredient.quantity, ingredient.unit);

            if (available == null
                    || available + 1e-6 < required) {
                missing++;
            }
        }

        return missing;
    }

    public static boolean isStrictMatch(
            List<RecipeIngredient> needed,
            Map<String, Double> stock) {

        return needed != null
                && !needed.isEmpty()
                && countMissing(needed, stock) == 0;
    }

    public static String diagnose(
            List<RecipeIngredient> needed,
            Map<String, Double> stock) {

        StringBuilder result = new StringBuilder();

        result.append("EGG NORMALISATION TEST: ")
                .append(normalizeName("eggs"))
                .append("\n");

        result.append("PANTRY EGG KEY TEST: ")
                .append(key("eggs", "pcs"))
                .append("\n\n");

        for (RecipeIngredient ingredient : needed) {
            String ingredientKey = key(
                    ingredient.name, ingredient.unit);

            Double available = stock.get(ingredientKey);

            double required = UnitConverter.toBase(
                    ingredient.quantity, ingredient.unit);

            result.append(ingredient.name)
                    .append(" | key=")
                    .append(ingredientKey)
                    .append(" | required=")
                    .append(required)
                    .append(" | available=")
                    .append(available == null
                            ? "NOT FOUND"
                            : available)
                    .append(" | ")
                    .append(available == null
                            || available + 1e-6 < required
                            ? "MISSING"
                            : "MATCHED")
                    .append("\n");
        }

        return result.toString();
    }
}

