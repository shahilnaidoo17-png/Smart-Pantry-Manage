# Smart Pantry Manager

An Android app (Java) that reduces food waste. The user records the ingredients they have at
home and the app suggests recipes they can cook **using only what they already have**.
A recipe is suggested only if *every* ingredient is present in at least the required quantity.

## Features
- Pantry CRUD: add, edit, delete ingredients (name, quantity, unit, optional expiry date)
- Pantry list (RecyclerView + custom adapter) with expiry highlighting
- 20 pre-loaded recipes (seeded on first launch)
- Suggested Recipes screen using the strict-matching rule, plus a separate optional "Almost there" view
- Recipe detail screen (ingredients + method)
- Settings (expiring-soon alert, ignore expired items when matching)
- Bottom navigation, input validation, empty-state messages

## Database choice: SQLite via Room
Pantry data is personal, small and needs to work offline, so a local on-device database is the
best fit. Room gives compile-time-checked SQL, DAOs and entities, with no server to host or
secure. Data persists across app restarts.

Tables: `pantry_items`, `recipes`, `recipe_ingredients` (FK -> recipes, cascade delete).

## Strict matching (`logic/RecipeMatcher.java`)
- Names are normalised (case, whitespace, simple plurals: "Tomatoes" == "tomato")
- Units convert to a base unit (1 kg covers 500 g; 1 cup = 240 ml)
- Duplicate pantry rows for one ingredient are summed
- Different unit kinds (grams vs pieces) are never assumed equal, so such a recipe is excluded
- Any missing or insufficient ingredient excludes the recipe

## Setup / run
1. Install Android Studio (Hedgehog or newer) with Android SDK 34.
2. `File > Open` and select the `SmartPantryManager` folder; let Gradle sync finish.
3. Create an emulator (API 24+) or connect a device with USB debugging.
4. Press **Run**. Recipes are seeded on first launch.

No Google Maps, mapping SDKs or location permissions are used.
