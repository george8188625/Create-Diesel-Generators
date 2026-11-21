package com.jesz.createdieselgenerators.world;

import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.CDGTags;
import com.jesz.createdieselgenerators.compat.kubejs.CDGKubeJSPlugin;
import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.synth.PerlinNoise;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.ModList;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class OilChunksSavedData extends SavedData {
    private final Map<ChunkPos, Integer> chunks = new HashMap<>();
    private final ServerLevel level;

    @Override
    public @NonNull CompoundTag save(CompoundTag compound, HolderLookup.@NonNull Provider registries) {
        ListTag lt = new ListTag();
        chunks.forEach((pos,amount) -> {
            CompoundTag c = new CompoundTag();
            c.put("x", IntTag.valueOf(pos.x));
            c.put("z", IntTag.valueOf(pos.z));
            c.put("Amountmb", IntTag.valueOf(amount));
            lt.add(c);
        });

        compound.put("OilChunks", lt);

        return compound;
    }

    private OilChunksSavedData(ServerLevel level) {
        this.level = level;
    }

    private static OilChunksSavedData load(ServerLevel level, CompoundTag tag, HolderLookup.Provider registries) {
        OilChunksSavedData sd = new OilChunksSavedData(level);

        NBTHelper.iterateCompoundList(tag.getList("OilChunks", Tag.TAG_COMPOUND), c -> {
            sd.chunks.put(new ChunkPos(c.getInt("x"), c.getInt("z")),
                    c.contains("Amountmb") ?
                            c.getInt("Amountmb") :
                            c.getInt("Amount") * 1000);
        });

        return sd;
    }

    public static OilChunksSavedData load(ServerLevel level) {
        return level.getDataStorage()
                .computeIfAbsent(new Factory<>(
                        () -> new OilChunksSavedData(level),
                        (tag, provider) -> OilChunksSavedData.load(level, tag, provider)),
                        "cdg_oil_chunks");
    }

    public void setChunkAmount(ChunkPos chunk, int amount) {
        chunks.put(chunk, amount);
        setDirty();
    }

    public void removeChunk(ChunkPos chunk) {
        chunks.remove(chunk);
        setDirty();
    }

    public int getChunkOilAmount(ChunkPos chunk) {
        if (chunks.containsKey(chunk))
            return chunks.get(chunk) > CDGConfig.OIL_CHUNK_INFINITE_THRESHOLD.get() ? Integer.MAX_VALUE : chunks.get(chunk);
        return getBaseOilAmount(level, chunk);
    }

    public static int getChunkOilAmount(ServerLevel level, ChunkPos chunk) {
        return load(level).getChunkOilAmount(chunk);
    }

    public static void setChunkOilAmount(ServerLevel level, ChunkPos chunk, int amount) {
        load(level).setChunkAmount(chunk, amount);
    }

    public static void removeChunk(ServerLevel level, ChunkPos chunk) {
        load(level).removeChunk(chunk);
    }

    private static final List<Integer> OCTAVES = List.of(-2, -1, 0, 1);

    public static int getBaseOilAmount(ServerLevel level, ChunkPos chunk) {
        long seed = level.getSeed();
        Collection<Holder<Biome>> biomes = getBiomesInChunk(level, chunk);
        if (ModList.get().isLoaded("kubejs")) {
            int amount = CDGKubeJSPlugin.calculateOilChunks(biomes, chunk, seed);
            if(amount != -1)
                return amount;
        }

        double scale = CDGConfig.OIL_CHUNK_SCALE.get();
        PerlinNoise noise = PerlinNoise.create(RandomSource.create(seed), OCTAVES);
        float amount = (float) (noise.getValue(chunk.x * scale, 0, chunk.z * scale) + 1) / 1.6f;

        boolean isHighInOil = false;
        boolean isDenied = false;
        for (Holder<Biome> biome : biomes) {
            if (biome.is(CDGTags.OIL_BIOMES))
                isHighInOil = true;
            if (biome.is(CDGTags.DENY_OIL_BIOMES))
                isDenied = true;
        }

        if ((isHighInOil && CDGConfig.DISABLE_HIGH_OIL_CHUNKS.get()) ||
                (!isHighInOil && CDGConfig.DISABLE_NORMAL_OIL_CHUNKS.get()))
            return 0;

        if (isDenied)
            return 0;

        int max = (int) (7_000_000 * CDGConfig.OIL_MULTIPLIER.get());
        if (isHighInOil)
            max = (int) (7_000_000 * CDGConfig.HIGH_OIL_MULTIPLIER.get());

        amount = (float) Math.pow(amount, 2);
        amount *= max;


        if (amount < CDGConfig.OIL_CHUNK_THRESHOLD.get())
            return 0;

        if (amount > CDGConfig.OIL_CHUNK_INFINITE_THRESHOLD.get())
            return Integer.MAX_VALUE;
        return (int) amount;
    }

    public static Set<Holder<Biome>> getBiomesInChunk(ServerLevel level, ChunkPos chunkPos) {
        Set<Holder<Biome>> list = new HashSet<>();
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int y = 60; y < 110; y++) {
                for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                    list.add(level.getBiome(new BlockPos(x, y, z)));
                }
            }
        }
        return list;
    }
}
