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

### Database schema

The application uses Room ORM with a local SQLite database named `smart_pantry.db`. It contains three entities:

| Table | Important fields | Purpose |
|---|---|---|
| `pantry_items` | `id`, `name`, `quantity`, `unit`, `expiryDate` | Stores the ingredients currently in the user's pantry. The ID is auto-generated, and the expiry date is optional. |
| `recipes` | `id` and recipe details | Stores the recipes available in the application. |
| `recipe_ingredients` | `id`, `recipeId`, `name`, `quantity`, `unit` | Stores the ingredients and quantities required for each recipe. |

The `recipe_ingredients.recipeId` field is a foreign key referencing `recipes.id`. This creates a one-to-many relationship: one recipe can have multiple required ingredients. The foreign key uses cascade deletion, so deleting a recipe also deletes its associated recipe-ingredient records.

`AppDatabase.java` configures the Room database and provides access to the pantry and recipe DAOs. Database operations use a single-thread executor to avoid blocking the Android UI thread.


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

## Automated tests

The recipe-matching logic is covered by JUnit unit tests in `app/src/test/java/com/example/smartpantry/logic/RecipeMatcherTest.java`.

The tests cover:
- Ingredient name normalisation, including plurals and capitalisation
- Strict matching when all ingredients and quantities are available
- Missing ingredients and insufficient quantities
- Combining duplicate pantry entries
- Compatible unit conversions and incompatible units
- Excluding expired items when the setting is enabled
- Ingredients without expiry dates

To run the tests on Windows, open PowerShell in the project folder and execute:

```powershell
.\gradlew.bat test --no-daemon --console=plain
```

A successful run displays `BUILD SUCCESSFUL`. Test results are available under `app/build/reports/tests/`.

