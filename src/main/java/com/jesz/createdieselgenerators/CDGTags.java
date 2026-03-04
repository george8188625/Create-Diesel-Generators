package com.jesz.createdieselgenerators;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

public class CDGTags {

    public static final TagKey<Biome> OIL_BIOMES = TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), CreateDieselGenerators.rl("oil_biomes"));
    public static final TagKey<Biome> DENY_OIL_BIOMES = TagKey.create(ForgeRegistries.BIOMES.getRegistryKey(), CreateDieselGenerators.rl("deny_oil_biomes"));
    public static final TagKey<Block> HEAT_SOURCES = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), new ResourceLocation("farmersdelight:heat_sources"));
    public static final TagKey<Block> LIGHTER_LIGHTABLE = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), CreateDieselGenerators.rl("lighter_lightable"));
    public static final TagKey<Block> PUMPJACK_PIPE = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), CreateDieselGenerators.rl("pumpjack_pipe"));
    public static final TagKey<Block> OIL_DEPOSIT = TagKey.create(ForgeRegistries.BLOCKS.getRegistryKey(), CreateDieselGenerators.rl("oil_deposit"));
    public static final TagKey<Fluid> PUMPJACK_OUTPUT = TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), CreateDieselGenerators.rl("pumpjack_output"));
    public static final TagKey<Item> WOOD_DUST = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation("forge:dusts/wood"));
}
