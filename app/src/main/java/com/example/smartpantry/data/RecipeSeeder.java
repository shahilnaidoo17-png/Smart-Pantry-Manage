package com.example.smartpantry.data;

import java.util.ArrayList;
import java.util.List;

/** Pre-loads 20 recipes the first time the app runs (only if the table is empty). */
public class RecipeSeeder {

    public static void seedIfEmpty(AppDatabase db) {
        RecipeDao dao = db.recipeDao();
        if (dao.count() > 0) return;

        add(dao, "Cheese Omelette", "1. Beat the eggs.\n2. Melt butter in a pan.\n3. Pour in eggs, add cheese, fold and serve.",
                "egg:3:pcs", "cheese:50:g", "butter:10:g");
        add(dao, "Tomato Pasta", "1. Boil the pasta.\n2. Fry garlic in olive oil, add chopped tomatoes and simmer 10 min.\n3. Toss with pasta.",
                "pasta:200:g", "tomato:4:pcs", "garlic:2:pcs", "olive oil:2:tbsp");
        add(dao, "Egg Fried Rice", "1. Cook the rice and let it cool.\n2. Fry onion, push aside and scramble eggs.\n3. Add rice and soy sauce, stir-fry 5 min.",
                "rice:200:g", "egg:2:pcs", "onion:1:pcs", "soy sauce:2:tbsp");
        add(dao, "Pancakes", "1. Whisk flour, milk, egg and sugar into a batter.\n2. Fry ladlefuls in a hot pan until golden on both sides.",
                "flour:200:g", "milk:300:ml", "egg:1:pcs", "sugar:1:tbsp");
        add(dao, "Garlic Butter Toast", "1. Mash crushed garlic into soft butter.\n2. Spread on bread.\n3. Toast under the grill until golden.",
                "bread:4:pcs", "butter:40:g", "garlic:2:pcs");
        add(dao, "Mashed Potatoes", "1. Boil peeled potatoes until soft.\n2. Drain and mash with butter and warm milk.",
                "potato:4:pcs", "butter:30:g", "milk:100:ml");
        add(dao, "Chicken and Rice", "1. Brown diced chicken with onion.\n2. Add rice and 400 ml water.\n3. Cover and cook 15 min.",
                "chicken breast:300:g", "rice:200:g", "onion:1:pcs");
        add(dao, "Simple Tomato Soup", "1. Soften onion and garlic.\n2. Add tomatoes and stock, simmer 20 min.\n3. Blend until smooth.",
                "tomato:6:pcs", "onion:1:pcs", "garlic:2:pcs", "vegetable stock:500:ml");
        add(dao, "Banana Smoothie", "1. Peel bananas.\n2. Blend with milk and honey until smooth.",
                "banana:2:pcs", "milk:250:ml", "honey:1:tbsp");
        add(dao, "Scrambled Eggs on Toast", "1. Toast the bread.\n2. Scramble eggs gently in butter.\n3. Pile onto toast.",
                "egg:3:pcs", "bread:2:pcs", "butter:10:g");
        add(dao, "Cheese Sandwich", "1. Butter the bread.\n2. Add cheese and close the sandwich.\n3. Fry on both sides until melted.",
                "bread:2:pcs", "cheese:40:g", "butter:10:g");
        add(dao, "Lentil Curry", "1. Fry onion with curry powder.\n2. Add lentils, chopped tomatoes and 500 ml water.\n3. Simmer 25 min.",
                "lentil:200:g", "onion:1:pcs", "tomato:2:pcs", "curry powder:2:tsp");
        add(dao, "Bean Burrito", "1. Warm the beans with onion.\n2. Spoon onto tortillas, add cheese.\n3. Roll up and toast in a dry pan.",
                "tortilla:2:pcs", "kidney bean:200:g", "cheese:50:g", "onion:1:pcs");
        add(dao, "Veggie Stir-Fry", "1. Cook the rice.\n2. Stir-fry sliced carrots and onion.\n3. Add soy sauce and serve over rice.",
                "carrot:2:pcs", "onion:1:pcs", "soy sauce:2:tbsp", "rice:150:g");
        add(dao, "Roast Potatoes", "1. Cut potatoes, toss with oil, garlic and salt.\n2. Roast at 200C for 40 min.",
                "potato:4:pcs", "olive oil:2:tbsp", "garlic:2:pcs", "salt:1:tsp");
        add(dao, "Oat Porridge", "1. Simmer oats in milk for 5 min, stirring.\n2. Drizzle with honey.",
                "oat:80:g", "milk:300:ml", "honey:1:tbsp");
        add(dao, "French Toast", "1. Whisk egg, milk and sugar.\n2. Dip bread slices.\n3. Fry until golden.",
                "bread:4:pcs", "egg:2:pcs", "milk:100:ml", "sugar:1:tbsp");
        add(dao, "Tuna Pasta", "1. Boil the pasta.\n2. Warm garlic in olive oil, flake in the tuna.\n3. Toss with pasta.",
                "pasta:200:g", "tuna:1:pcs", "olive oil:1:tbsp", "garlic:1:pcs");
        add(dao, "Simple Guacamole", "1. Mash avocados.\n2. Stir in diced tomato, lemon juice and salt.",
                "avocado:2:pcs", "lemon:1:pcs", "tomato:1:pcs", "salt:0.5:tsp");
        add(dao, "Rice Pudding", "1. Simmer rice in milk for 30 min, stirring often.\n2. Stir in sugar.",
                "rice:100:g", "milk:500:ml", "sugar:3:tbsp");
    }

    /** ingredientSpecs use the format "name:quantity:unit". */
    private static void add(RecipeDao dao, String name, String steps, String... ingredientSpecs) {
        long recipeId = dao.insertRecipe(new Recipe(name, steps));
        List<RecipeIngredient> list = new ArrayList<>();
        for (String spec : ingredientSpecs) {
            String[] p = spec.split(":");
            list.add(new RecipeIngredient(recipeId, p[0], Double.parseDouble(p[1]), p[2]));
        }
        dao.insertIngredients(list);
    }
}
