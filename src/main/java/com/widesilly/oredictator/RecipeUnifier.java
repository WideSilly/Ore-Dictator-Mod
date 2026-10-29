package com.widesilly.oredictator;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class RecipeUnifier {

    public static void unifyRecipes() {
        // 1. Unify Crafting Recipes (Shaped & Shapeless)
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (recipe == null) continue;

            // Unify recipe inputs to support Ore Dictionary equivalents
            NonNullList<Ingredient> ingredients = recipe.getIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ing = ingredients.get(i);
                if (ing == null || ing == Ingredient.EMPTY) continue;

                // Check if any matching stack in this ingredient belongs to a unified ore category
                for (ItemStack stack : ing.getMatchingStacks()) {
                    String oreName = getMatchingOreName(stack);
                    if (oreName != null) {
                        NonNullList<ItemStack> oreItems = OreDictionary.getOres(oreName);
                        ingredients.set(i, Ingredient.fromStacks(oreItems.toArray(new ItemStack[0])));
                        break;
                    }
                }
            }

            // Unify recipe output to strictly use the preferred mod variant
            ItemStack output = recipe.getRecipeOutput();
            ItemStack unifiedOutput = getUnifiedEquivalent(output);
            if (!unifiedOutput.isEmpty() && !isSameItem(output, unifiedOutput)) {
                setRecipeOutput(recipe, unifiedOutput);
            }
        }

        // 2. Unify Furnace Smelting Recipes
        Map<ItemStack, ItemStack> smeltingList = FurnaceRecipes.instance().getSmeltingList();
        Map<ItemStack, ItemStack> replacements = new HashMap<>();

        for (Map.Entry<ItemStack, ItemStack> entry : smeltingList.entrySet()) {
            ItemStack input = entry.getKey();
            ItemStack output = entry.getValue();

            ItemStack unifiedInput = getUnifiedEquivalent(input);
            ItemStack unifiedOutput = getUnifiedEquivalent(output);

            if (!unifiedInput.isEmpty() || !unifiedOutput.isEmpty()) {
                ItemStack newKey = !unifiedInput.isEmpty() ? unifiedInput : input;
                ItemStack newVal = !unifiedOutput.isEmpty() ? unifiedOutput : output;
                replacements.put(newKey, newVal);
            }
        }

        for (Map.Entry<ItemStack, ItemStack> replacementsEntry : replacements.entrySet()) {
            FurnaceRecipes.instance().addSmeltingRecipe(replacementsEntry.getKey(), replacementsEntry.getValue(), 0.7f);
        }
    }

    private static String getMatchingOreName(ItemStack stack) {
        if (stack.isEmpty()) return null;
        for (int oreId : OreDictionary.getOreIDs(stack)) {
            String oreName = OreDictionary.getOreName(oreId);
            if (ConfigManager.config.ores.containsKey(oreName)) {
                return oreName;
            }
        }
        return null;
    }

    private static ItemStack getUnifiedEquivalent(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;

        for (int oreId : OreDictionary.getOreIDs(stack)) {
            String oreName = OreDictionary.getOreName(oreId);
            
            if (ConfigManager.config.ores.containsKey(oreName)) {
                ItemStack preferredStack = ConfigManager.getSelectedItemStack(oreName);
                
                if (!preferredStack.isEmpty() && !isSameItem(stack, preferredStack)) {
                    ItemStack result = preferredStack.copy();
                    result.setCount(stack.getCount());
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static void setRecipeOutput(IRecipe recipe, ItemStack newOutput) {
        try {
            Field f = getField(recipe.getClass(), "recipeOutput", "field_77575_e");
            if (f != null) {
                f.setAccessible(true);
                f.set(recipe, newOutput);
            }
        } catch (Exception ignored) {}
    }

    private static Field getField(Class<?> clazz, String mcpName, String srgName) {
        try {
            return clazz.getDeclaredField(mcpName);
        } catch (NoSuchFieldException e1) {
            try {
                return clazz.getDeclaredField(srgName);
            } catch (NoSuchFieldException e2) {
                return null;
            }
        }
    }

    private static boolean isSameItem(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
    }
}