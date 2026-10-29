package com.widesilly.oredictator;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid = OreDictator.MODID)
public class ItemHider {

    // Runs during postInit or via registry event hooks to strip unselected items from creative search
    public static void hideUnselectedItems() {
        for (String oreName : ConfigManager.config.ores.keySet()) {
            ItemStack selectedStack = ConfigManager.getSelectedItemStack(oreName);
            if (selectedStack.isEmpty()) continue;

            // Loop through all items registered to this Ore Dictionary name
            for (ItemStack oreStack : OreDictionary.getOres(oreName)) {
                if (oreStack.isEmpty()) continue;

                // If this item variant does NOT match the user's preferred mod choice...
                if (!isSameItem(oreStack, selectedStack)) {
                    // Remove it from the creative tab list by changing its creative tab to null or clearing sub-items
                    disableItemInTabs(oreStack);
                }
            }
        }
    }

    private static void disableItemInTabs(ItemStack stack) {
        try {
            Item item = stack.getItem();
            // Force the item to not show up in any creative tabs
            item.setCreativeTab(null);
        } catch (Exception ignored) {}
    }

    private static boolean isSameItem(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getMetadata() == b.getMetadata();
    }
}