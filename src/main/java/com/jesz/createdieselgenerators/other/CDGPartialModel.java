package com.jesz.createdieselgenerators.other;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ForgeModelBakery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CDGPartialModel {

    private static final List<CDGPartialModel> ALL = new ArrayList<>();

    protected final ResourceLocation modelLocation;
    protected BakedModel bakedModel;

    public CDGPartialModel(ResourceLocation modelLocation) {
        this.modelLocation = modelLocation;
        ALL.add(this);
    }

    public static void onModelRegistry(ModelRegistryEvent event) {
        for (CDGPartialModel partial : ALL)
            ForgeModelBakery.addSpecialModel(partial.getLocation());
    }

    public static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModelRegistry();
        for (CDGPartialModel partial : ALL)
            partial.set(modelRegistry.get(partial.getLocation()));
    }

    protected void set(BakedModel bakedModel) {
        this.bakedModel = bakedModel;
    }

    public ResourceLocation getLocation() {
        return modelLocation;
    }

    public BakedModel get() {
        return bakedModel;
    }


}
