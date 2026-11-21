package com.jesz.createdieselgenerators.compat.kubejs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.tools.lighter.LighterModel;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.plugin.ClassFilter;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;

import java.util.Collection;
import java.util.List;

public class CDGKubeJSPlugin implements KubeJSPlugin {
    public static EventGroup GROUP = EventGroup.of("CDGEvents");
    public static EventHandler MOLDS = GROUP.startup("molds", () -> MoldEventJS.class);
    public static EventHandler OIL_CHUNKS = GROUP.server("oilAmount", () -> GetChunkOilAmountEventJS.class);
    public static EventHandler LIGHTER_SKINS = GROUP.client("lighterSkins", () -> LighterSkinsEventJS.class);

    static {
        OIL_CHUNKS.hasResult();
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(GROUP);
    }

    @Override
    public void registerClasses(ClassFilter filter) {
        filter.allow("com.jesz.createdieselgenerators");
        filter.deny("com.jesz.createdieselgenerators.mixins");
        filter.deny(CDGKubeJSPlugin.class);
    }

    public static void registerMolds(){
        MoldEventJS event = new MoldEventJS();
        MOLDS.post(event);
    }

    @Override
    public void initStartup() {
        registerMolds();
    }

    public static int calculateOilChunks(Collection<Holder<Biome>> biomes, ChunkPos chunkPos, long seed) {
        if (!OIL_CHUNKS.hasListeners())
            return -1;

        GetChunkOilAmountEventJS event = new GetChunkOilAmountEventJS();
        event.chunkPos = chunkPos;
        event.seed = seed;
        event.biomes = biomes.stream().map(Holder::getRegisteredName).toArray(String[]::new);

        EventResult result = OIL_CHUNKS.post(event);
        return result.value() == null ? -1 : (int) result.value();
    }

    public static void registerLighterSkins() {
        LighterSkinsEventJS event = new LighterSkinsEventJS();
        LIGHTER_SKINS.post(event);
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        CDGKubeJSPlugin.registerLighterSkins();

        MoldEventJS.addedMolds.forEach((rl, name) -> {
            generator.json(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "models/item/mold/"+rl.getPath()), generateTextureModel(ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "item/mold/"+rl.getPath())));
        });

        LighterSkinsEventJS.addedIds.forEach((name, id) -> {
            generator.json(CreateDieselGenerators.rl("models/item/lighter/"+id), generateLighterSkinModel(id, LighterModel.LighterState.CLOSED));
            generator.json(CreateDieselGenerators.rl("models/item/lighter/"+id+"_open"), generateLighterSkinModel(id, LighterModel.LighterState.OPEN));
            generator.json(CreateDieselGenerators.rl("models/item/lighter/"+id+"_ignited"), generateLighterSkinModel(id, LighterModel.LighterState.IGNITED));
        });
    }

    @Override
    public void generateLang(LangKubeEvent event) {
        MoldEventJS.addedMolds.forEach((rl, name) -> {
            event.add("mold." + rl.getNamespace() + "." + rl.getPath(), name);
        });
    }

    JsonElement generateTextureModel(ResourceLocation rl) {
        JsonObject object = new JsonObject();
        object.add("parent", new JsonPrimitive("minecraft:item/generated"));
        JsonObject texturesObject = new JsonObject();
        texturesObject.add("layer0", new JsonPrimitive(rl.toString()));
        object.add("textures", texturesObject);
        return object;
    }

    JsonElement generateLighterSkinModel(String id, LighterModel.LighterState state){
        JsonObject object = new JsonObject();
        object.add("parent", new JsonPrimitive("minecraft:item/generated"));
        JsonObject texturesObject = new JsonObject();
        texturesObject.add("layer0", new JsonPrimitive("kubejs:item/lighter/"+id+state.getSuffix()));
        object.add("textures", texturesObject);
        return object;
    }
}
