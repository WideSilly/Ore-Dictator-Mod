package com.widesilly.oredictator;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Map;
import java.util.Random;

public class OreWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) return; // Overworld only

        for (Map.Entry<String, OreConfig.OreEntry> entry : ConfigManager.config.ores.entrySet()) {
            String oreName = entry.getKey();
            
            if (!oreName.startsWith("ore")) continue;

            OreConfig.OreEntry oreData = entry.getValue();
            ItemStack selectedStack = ConfigManager.getSelectedItemStack(oreName);

            if (selectedStack.isEmpty() || !(selectedStack.getItem() instanceof ItemBlock)) continue;

            Block block = ((ItemBlock) selectedStack.getItem()).getBlock();
            IBlockState state = block.getDefaultState();

            int veinSize = oreData.vein_size;
            int minHeight = oreData.min_height;
            int maxHeight = oreData.max_height;
            int spawnTries = oreData.spawn_frequency;

            if (veinSize <= 0) veinSize = 6;
            if (maxHeight <= 0) maxHeight = 48;
            if (spawnTries <= 0) spawnTries = 8;

            WorldGenMinable minable = new WorldGenMinable(state, veinSize);

            for (int i = 0; i < spawnTries; i++) {
                int posX = chunkX * 16 + random.nextInt(16);
                int posY = minHeight + random.nextInt(maxHeight - minHeight);
                int posZ = chunkZ * 16 + random.nextInt(16);

                minable.generate(world, random, new BlockPos(posX, posY, posZ));
            }
        }
    }
}