package com.widesilly.oredictator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OreConfig {
    public Map<String, OreEntry> ores = new TreeMap<>();

    public OreConfig() {}

    public static class OreEntry {
        public String _available_mods;
        public String selected_mod;
        
        // World-gen configuration parameters per ore
        public int vein_size;
        public int min_height;
        public int max_height;
        public int spawn_frequency;

        public OreEntry() {}

        public OreEntry(List<String> availableMods, String selectedMod, int veinSize, int minHeight, int maxHeight, int spawnFreq) {
            this._available_mods = "Options: " + String.join(", ", availableMods);
            this.selected_mod = selectedMod;
            this.vein_size = veinSize;
            this.min_height = minHeight;
            this.max_height = maxHeight;
            this.spawn_frequency = spawnFreq;
        }
    }
}