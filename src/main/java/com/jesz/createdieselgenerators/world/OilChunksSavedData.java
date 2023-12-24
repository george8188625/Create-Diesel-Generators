package com.jesz.createdieselgenerators.world;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.foundation.utility.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class OilChunksSavedData extends SavedData {

    Map<ChunkPos, Integer> chunks = new HashMap<>();
    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag lt = new ListTag();
        chunks.forEach((pos,amount) -> {
            CompoundTag c = new CompoundTag();
            c.put("x", IntTag.valueOf(pos.x));
            c.put("z", IntTag.valueOf(pos.z));
            c.put("Amount", IntTag.valueOf(amount));
            lt.add(c);
        });

        compound.put("OilChunks", lt);

        return compound;
    }

    private OilChunksSavedData() {

    }

    private static OilChunksSavedData load(CompoundTag compound){
        OilChunksSavedData sd = new OilChunksSavedData();

        sd.chunks = new HashMap<>();
        NBTHelper.iterateCompoundList(compound.getList("OilChunks", Tag.TAG_COMPOUND), c -> {
            sd.chunks.put(new ChunkPos(c.getInt("x"), c.getInt("z")), c.getInt("Amount"));
        });

        return sd;
    }

    public static OilChunksSavedData load(ServerLevel level){
        return level.getDataStorage().computeIfAbsent(OilChunksSavedData::load, OilChunksSavedData::new, "cdg_oil_chunks");
    }
    public void setChunkAmount(ChunkPos chunk, int amount){
        if(chunks.containsKey(chunk))
            chunks.replace(chunk, amount);
        else
            chunks.put(chunk, amount);

        setDirty();
    }
    public void removeChunkAmount(ChunkPos chunk){
        chunks.remove(chunk);
        setDirty();
    }
    public int getChunkOilAmount(ChunkPos chunk){
        if(chunks.containsKey(chunk))
            return ConfigRegistry.OIL_DEPOSITS_INFINITE.get() ? Integer.MAX_VALUE : chunks.get(chunk);
        return -1;
    }

}
