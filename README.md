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

## Screen flow and navigation

The application is organised around five main screens:

1. **Pantry List (`PantryListActivity`)** â€” displays saved pantry ingredients and provides access to adding, editing, and deleting items.
2. **Add/Edit Ingredient (`AddEditItemActivity`)** â€” allows users to enter an ingredient's name, quantity, unit, and optional expiry date. Input validation helps prevent invalid entries.
3. **Suggested Recipes (`SuggestedRecipesActivity`)** â€” compares available pantry stock with recipe requirements. Complete matches appear under the recipes the user can make, while incomplete matches can be viewed separately.
4. **Recipe Details (`RecipeDetailActivity`)** â€” displays a selected recipe's required ingredients and preparation method.
5. **Settings (`SettingsActivity`)** â€” lets users configure expiry-related preferences.

Navigation uses Android activities and intents, with `NavHelper` assisting with navigation between the main sections. Pantry and recipe data are stored locally using Room and SQLite.

### Typical user journey

`Pantry List â†’ Add/Edit Ingredient â†’ Pantry List â†’ Suggested Recipes â†’ Recipe Details`

Users can also open Settings to change their preferences. When pantry contents change, the recipe suggestions can reflect the updated ingredients and quantities.


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

### Latest test verification

The recipe-matching unit test suite was run with Gradle using `--rerun-tasks`. The verified result was 13 tests, 0 failures, 0 errors, and 0 skipped tests. Both debug and release test tasks completed successfully.


## Android configuration

The project is an Android application written in Java and built with Gradle.

- **Application ID:** `com.example.smartpantry`
- **Namespace:** `com.example.smartpantry`
- **App version:** 1.0
- **Compile SDK:** Android API 34
- **Target SDK:** Android API 34
- **Minimum SDK:** Android API 24 (Android 7.0)
- **Java compatibility:** Java 8
- **UI libraries:** AndroidX AppCompat, Material Components, and RecyclerView
- **Local database:** Room 2.6.1 with SQLite
- **Unit testing:** JUnit 4.13.2

The minimum SDK is API 24, allowing the application to run on Android 7.0 and newer devices. The app does not declare location or mapping permissions because its features do not require geographic location.

## Assignment submission

The repository includes the assignment report and screenshots.

- **Report:** `402413326_Naidoo_MobileAppDev700_Report.docx`
- **Screenshots:** `assignment_screenshots/`
- **Application source:** `app/src/main/`
- **Unit tests:** `app/src/test/`

### Build verification

The prepared submission was extracted and tested using the Gradle wrapper. After configuring the Android SDK path in the extracted copy, both debug and release unit-test tasks completed successfully.

Run the tests on Windows with:

```powershell
.\gradlew.bat test --no-daemon --console=plain
```
