package com.widesilly.oredictator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    public static OreConfig config = new OreConfig();

    public static void init(File configDir) {
        configFile = new File(configDir, "oredict_choices.json");
        loadOrGenerateConfig();
    }

    public static void loadOrGenerateConfig() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                config = GSON.fromJson(reader, OreConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        boolean needsSave = !configFile.exists();

        for (String oreName : OreDictionary.getOreNames()) {
            if (!isTargetOreType(oreName)) {
                continue;
            }

            NonNullList<ItemStack> items = OreDictionary.getOres(oreName);
            if (items.isEmpty()) continue;

            List<String> providingMods = new ArrayList<>();

            for (ItemStack stack : items) {
                if (!stack.isEmpty() && stack.getItem().getRegistryName() != null) {
                    String modId = stack.getItem().getRegistryName().getResourceDomain();
                    if (!providingMods.contains(modId)) {
                        providingMods.add(modId);
                    }
                }
            }

            Collections.sort(providingMods);

            if (!providingMods.isEmpty()) {
                if (!config.ores.containsKey(oreName)) {
                    String defaultChoice = providingMods.get(0);
                    
                    int vein = getDefaultVeinSize(oreName);
                    int min = 0;
                    int max = getDefaultMaxHeight(oreName);
                    int freq = getDefaultSpawnFreq(oreName);

                    config.ores.put(oreName, new OreConfig.OreEntry(providingMods, defaultChoice, vein, min, max, freq));
                    needsSave = true;
                } else {
                    config.ores.get(oreName)._available_mods = "Options: " + String.join(", ", providingMods);
                }
            }
        }

        if (needsSave) {
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack getSelectedItemStack(String oreName) {
        if (!config.ores.containsKey(oreName)) return ItemStack.EMPTY;

        String chosenModId = config.ores.get(oreName).selected_mod;
        NonNullList<ItemStack> items = OreDictionary.getOres(oreName);

        for (ItemStack stack : items) {
            if (!stack.isEmpty() && stack.getItem().getRegistryName() != null) {
                if (stack.getItem().getRegistryName().getResourceDomain().equalsIgnoreCase(chosenModId)) {
                    return stack;
                }
            }
        }

        return items.isEmpty() ? ItemStack.EMPTY : items.get(0);
    }

    private static boolean isTargetOreType(String name) {
        return name.startsWith("ore") ||
               name.startsWith("ingot") ||
               name.startsWith("nugget") ||
               name.startsWith("dust") ||
               name.startsWith("gem");
    }

    private static int getDefaultVeinSize(String oreName) {
        String lower = oreName.toLowerCase();
        if (lower.contains("coal")) return 16;
        if (lower.contains("iron")) return 8;
        if (lower.contains("gold")) return 8;
        if (lower.contains("diamond")) return 7;
        if (lower.contains("redstone")) return 8;
        return 6;
    }

    private static int getDefaultMaxHeight(String oreName) {
        String lower = oreName.toLowerCase();
        if (lower.contains("diamond")) return 16;
        if (lower.contains("gold")) return 32;
        if (lower.contains("redstone")) return 16;
        if (lower.contains("iron")) return 64;
        if (lower.contains("coal")) return 128;
        return 48;
    }

    private static int getDefaultSpawnFreq(String oreName) {
        String lower = oreName.toLowerCase();
        if (lower.contains("coal")) return 20;
        if (lower.contains("iron")) return 20;
        if (lower.contains("gold")) return 2;
        if (lower.contains("diamond")) return 1;
        return 8;
    }
}