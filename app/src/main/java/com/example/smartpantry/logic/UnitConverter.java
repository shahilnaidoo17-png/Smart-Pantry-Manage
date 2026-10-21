package com.example.smartpantry.logic;

import java.util.Locale;

/** Converts quantities to a base unit (g, ml, or "each") so different units can be compared. */
public class UnitConverter {

    public static final String MASS = "mass";
    public static final String VOLUME = "volume";
    public static final String COUNT = "count";

    public static String dimension(String unit) {
        switch (clean(unit)) {
            case "g": case "kg": case "mg": case "oz": case "lb":
                return MASS;
            case "ml": case "l": case "cup": case "tbsp": case "tsp":
                return VOLUME;
            default:
                return COUNT;
        }
    }

    /** Quantity expressed in grams, millilitres, or pieces. */
    public static double toBase(double quantity, String unit) {
        switch (clean(unit)) {
            case "kg": return quantity * 1000;
            case "mg": return quantity / 1000;
            case "oz": return quantity * 28.35;
            case "lb": return quantity * 453.59;
            case "l": return quantity * 1000;
            case "cup": return quantity * 240;
            case "tbsp": return quantity * 15;
            case "tsp": return quantity * 5;
            default: return quantity; // g, ml, pcs
        }
    }

    private static String clean(String unit) {
        return unit == null ? "" : unit.toLowerCase(Locale.ROOT).trim();
    }
}
