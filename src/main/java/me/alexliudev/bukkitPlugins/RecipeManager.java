package me.alexliudev.bukkitPlugins;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class RecipeManager implements AutoCloseable {
    private final List<NamespacedKey> registeredRecipes = new ArrayList<>();
    public void embeddedRecipesInit() {
        if (!Bukkit.isPrimaryThread()) // 判断是不是 Server Thread 调用
            throw new IllegalStateException("Init RecipeManager action must be init by primary thread.");

        this.registerRecipe(new FurnaceRecipe(new NamespacedKey(SMPHelper.getHelper(), "leatherModify"),
                new ItemStack(Material.LEATHER), Material.ROTTEN_FLESH, 1, 40));

        this.registerRecipe(new FurnaceRecipe(new NamespacedKey(SMPHelper.getHelper(), "railModify"),
                new ItemStack(Material.IRON_NUGGET), Material.RAIL, 0.1f, 25));

        this.registerRecipe(new FurnaceRecipe(new NamespacedKey(SMPHelper.getHelper(), "poweredRailModify"),
                new ItemStack(Material.GOLD_NUGGET), Material.POWERED_RAIL, 0.1f, 25));

    }

    public void registerRecipe(Recipe recipe) {
        if (!(recipe instanceof Keyed)) // 判断有没有Namespaced Key
            throw new IllegalArgumentException("Register recipe must have namespace key.");
        if (!Bukkit.isPrimaryThread()) // 判断是不是 Server Thread 调用
            throw new IllegalStateException("Register recipe method must be called on primary thread.");
        if (Bukkit.getRecipe(((Keyed) recipe).getKey()) != null) // 判断是否已经有同样的Id已经注册了
            throw new IllegalStateException("Recipe's namespace key already registered.");

        registeredRecipes.add(((Keyed) recipe).getKey());
        Bukkit.addRecipe(recipe);
    }

    public void configCraftingRecipesInit() {
        List<?> list = SMPHelper.getHelper().getConfig().getList("Recipe.craftingRecipes");
        if (list == null)
            throw new RuntimeException("Configuration illegal");
        List<HashMap<String, Object>> recipes = (List<HashMap<String, Object>>) list;
        for (HashMap<String, Object> recipeConf : recipes) {
            ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(SMPHelper.getHelper(), String.valueOf(recipeConf.get("namespaced_key"))),
                    new ItemStack(Objects.requireNonNull(Material.getMaterial(String.valueOf(recipeConf.get("result"))))));
            String firstRow = String.valueOf(recipeConf.get("firstRow"));
            String secondRow = String.valueOf(recipeConf.get("secondRow"));
            String thirdRow = String.valueOf(recipeConf.get("thirdRow"));
            recipe.shape(firstRow, secondRow, thirdRow);
            List<HashMap<String,String>> ingredients = (List<HashMap<String,String>>) recipeConf.get("ingredients");
            for (HashMap<String,String> ingredient : ingredients) {
                recipe.setIngredient(ingredient.get("id").charAt(0), Objects.requireNonNull(Material.getMaterial(ingredient.get("material"))));
            }
            this.registerRecipe(recipe);
        }
    }

    @Override
    public void close() {
        if (!Bukkit.isPrimaryThread()) // 判断是不是 Server Thread 调用
            throw new IllegalStateException("Close RecipeManager action must be init by primary thread.");
        for (NamespacedKey key : registeredRecipes) {
            Bukkit.removeRecipe(key);
        }
    }
}
