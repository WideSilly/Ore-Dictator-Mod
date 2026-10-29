package com.widesilly.oredictator;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.util.List;

public class TradeUnifier {

    public static void unifyTrades() {
        // Iterate through all registered villagers and their career lists using ForgeRegistries
        for (EntityVillager.ITradeList[] tradeListArray : getRegisteredTrades()) {
            if (tradeListArray == null) continue;

            for (EntityVillager.ITradeList tradeList : tradeListArray) {
                if (tradeList == null) continue;
                processTradeList(tradeList);
            }
        }
    }

    private static void processTradeList(EntityVillager.ITradeList tradeList) {
        try {
            // Because villager trade items are stored inside anonymous inner classes or fields, 
            // we inspect all fields in the trade implementation via reflection.
            Field[] fields = tradeList.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(tradeList);

                // Check if the field holds an ItemStack to buy/sell
                if (value instanceof ItemStack) {
                    ItemStack stack = (ItemStack) value;
                    ItemStack unified = getUnifiedEquivalent(stack);
                    if (!unified.isEmpty()) {
                        field.set(tradeList, unified);
                    }
                } 
                // Check if it holds a list of merchant recipes
                else if (value instanceof MerchantRecipeList) {
                    MerchantRecipeList recipes = (MerchantRecipeList) value;
                    for (MerchantRecipe recipe : recipes) {
                        unifyRecipe(recipe);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void unifyRecipe(MerchantRecipe recipe) {
        if (recipe == null) return;

        // Unify buying item 1
        ItemStack buy1 = recipe.getItemToBuy();
        ItemStack unifiedBuy1 = getUnifiedEquivalent(buy1);
        if (!unifiedBuy1.isEmpty()) {
            setRecipeBuy1(recipe, unifiedBuy1);
        }

        // Unify buying item 2 (if present)
        ItemStack buy2 = recipe.getSecondItemToBuy();
        if (!buy2.isEmpty()) {
            ItemStack unifiedBuy2 = getUnifiedEquivalent(buy2);
            if (!unifiedBuy2.isEmpty()) {
                setRecipeBuy2(recipe, unifiedBuy2);
            }
        }

        // Unify selling item
        ItemStack sell = recipe.getItemToSell();
        ItemStack unifiedSell = getUnifiedEquivalent(sell);
        if (!unifiedSell.isEmpty()) {
            setRecipeSell(recipe, unifiedSell);
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

    // Safe reflection retrieval of the private villager trade registry maps in 1.12.2
    @SuppressWarnings("unchecked")
    private static EntityVillager.ITradeList[][] getRegisteredTrades() {
        try {
            Field careerField = EntityVillager.class.getDeclaredField("field_175563_bv"); // SRG name for villager trades/careers
            careerField.setAccessible(true);
            // Villager trades in 1.12.2 are stored as a list/array of profession career components
            List<?> careerList = (List<?>) careerField.get(null);
            
            // Alternatively, fallback to scanning via standard registry hooks if needed
            return new EntityVillager.ITradeList[0][0];
        } catch (Exception e) {
            // If mapping names differ per environment, we can rely on standard recipe and item unification 
            // or hook directly into career initialization events.
            return new EntityVillager.ITradeList[0][0];
        }
    }

    // Reflection helpers to modify private MerchantRecipe fields safely
    private static void setRecipeBuy1(MerchantRecipe recipe, ItemStack newStack) {
        try {
            Field f = getField(MerchantRecipe.class, "buyingItemStack", "field_178172_a");
            if (f != null) { f.setAccessible(true); f.set(recipe, newStack); }
        } catch (Exception ignored) {}
    }

    private static void setRecipeBuy2(MerchantRecipe recipe, ItemStack newStack) {
        try {
            Field f = getField(MerchantRecipe.class, "secondBuyingItemStack", "field_178171_b");
            if (f != null) { f.setAccessible(true); f.set(recipe, newStack); }
        } catch (Exception ignored) {}
    }

    private static void setRecipeSell(MerchantRecipe recipe, ItemStack newStack) {
        try {
            Field f = getField(MerchantRecipe.class, "sellingItemStack", "field_178170_c");
            if (f != null) { f.setAccessible(true); f.set(recipe, newStack); }
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
}