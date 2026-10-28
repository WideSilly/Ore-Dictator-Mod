package com.widesilly.oredictator;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Map;

public class RecipeUnifier {

    public static void unifyRecipes() {
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

        for (Map.Entry<ItemStack, ItemStack> entry : replacements.entrySet()) {
            FurnaceRecipes.instance().addSmeltingRecipe(entry.getKey(), entry.getValue(), 0.7f);
        }
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

    private static boolean isSameItem(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
    }
}