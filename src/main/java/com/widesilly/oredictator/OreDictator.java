package com.widesilly.oredictator;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = OreDictator.MODID, name = OreDictator.NAME, version = OreDictator.VERSION)
public class OreDictator {
    public static final String MODID = "oredictator";
    public static final String NAME = "Ore Dictator Mod";
    public static final String VERSION = "1.0.2";

    private static java.io.File configFolder;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configFolder = event.getModConfigurationDirectory();
        
        // Register the global ore-gen blocker to the Ore Gen Bus
        MinecraftForge.ORE_GEN_BUS.register(OreGenHandler.class);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register your clean exception world generator
        GameRegistry.registerWorldGenerator(new OreWorldGenerator(), 3);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ConfigManager.init(configFolder);
        RecipeUnifier.unifyRecipes();
        System.out.println("Ore Choice & Unification applied successfully!");
    }
}