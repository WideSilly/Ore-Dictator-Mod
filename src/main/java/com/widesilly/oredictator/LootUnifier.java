package com.widesilly.oredictator;

import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.util.Collection;

@Mod.EventBusSubscriber(modid = OreDictator.MODID)
public class LootUnifier {

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        LootTable table = event.getTable();
        if (table == null) return;

        try {
            // Access the private pools field in LootTable safely via reflection
            Field poolsField = getField(LootTable.class, "pools", "field_186466_a");
            if (poolsField == null) return;
            poolsField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Collection<LootPool> pools = (Collection<LootPool>) poolsField.get(table);
            if (pools == null) return;

            for (LootPool pool : pools) {
                Field entriesField = getField(LootPool.class, "lootEntries", "field_186453_a");
                if (entriesField == null) continue;
                entriesField.setAccessible(true);

                @SuppressWarnings("unchecked")
                Collection<LootEntry> entries = (Collection<LootEntry>) entriesField.get(pool);
                if (entries == null) continue;

                for (LootEntry entry : entries) {
                    if (entry instanceof LootEntryItem) {
                        // Access the item field inside LootEntryItem
                        Field itemField = getField(LootEntryItem.class, "item", "field_186368_a");
                        if (itemField == null) continue;
                        itemField.setAccessible(true);

                        net.minecraft.item.Item item = (net.minecraft.item.Item) itemField.get(entry);
                        if (item != null) {
                            ItemStack stack = new ItemStack(item, 1);
                            ItemStack unified = getUnifiedEquivalent(stack);

                            if (!unified.isEmpty() && unified.getItem() != item) {
                                itemField.set(entry, unified.getItem());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper to find fields across MCP names or SRG runtime names
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