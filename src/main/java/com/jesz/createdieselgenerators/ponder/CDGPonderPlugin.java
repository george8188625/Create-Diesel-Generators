package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGItems;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CDGPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateDieselGenerators.ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderPlugin.super.registerScenes(helper);

        helper.forComponents(CDGItems.DISTILLATION_CONTROLLER.getId())
                .addStoryBoard("distillation_tower", DistillationScene::scene);
        helper.forComponents(CDGBlocks.DIESEL_ENGINE.getId())
                .addStoryBoard("diesel_engine", DieselEngineScenes::small);
        helper.forComponents(CDGBlocks.MODULAR_DIESEL_ENGINE.getId())
                .addStoryBoard("large_diesel_engine", DieselEngineScenes::modular);
        helper.forComponents(CDGBlocks.BASIN_LID.getId())
                .addStoryBoard("basin_fermenting_station", BasinScenes::basin_lid);
        helper.forComponents(CDGBlocks.HUGE_DIESEL_ENGINE.getId())
                .addStoryBoard("huge_diesel_engine", DieselEngineScenes::huge);
        helper.forComponents(CDGBlocks.PUMPJACK_BEARING.getId(), CDGBlocks.PUMPJACK_CRANK.getId(), CDGBlocks.PUMPJACK_HEAD.getId())
                .addStoryBoard("pumpjack", PumpjackScene::scene);
        helper.forComponents(CDGBlocks.PUMPJACK_BEARING.getId(), CDGBlocks.PUMPJACK_CRANK.getId(), CDGBlocks.PUMPJACK_HEAD.getId(), CDGItems.OIL_SCANNER.getId())
                .addStoryBoard("pumpjack", OilChunkScene::scene);
        helper.forComponents(CDGBlocks.BURNER.getId())
                .addStoryBoard("burner", BurnerScenes::scene);
        helper.forComponents(CDGBlocks.CHEMICAL_TURRET.getId())
                .addStoryBoard("chemical_turret", TurretScenes::chemical)
                .addStoryBoard("automatic_turret", TurretScenes::automatic);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderPlugin.super.registerTags(helper);

        helper.addToTag(AllCreatePonderTags.KINETIC_SOURCES)
                .add(CDGBlocks.DIESEL_ENGINE.getId())
                .add(CDGBlocks.MODULAR_DIESEL_ENGINE.getId())
                .add(CDGBlocks.HUGE_DIESEL_ENGINE.getId());
        helper.addToTag(AllCreatePonderTags.KINETIC_APPLIANCES)
                .add(CDGBlocks.BASIN_LID.getId())
                .add(CDGBlocks.PUMPJACK_BEARING.getId())
                .add(CDGBlocks.CHEMICAL_TURRET.getId());
        helper.addToTag(AllCreatePonderTags.DISPLAY_SOURCES)
                .add(CDGBlocks.PUMPJACK_HOLE.getId());
        helper.addToTag(AllCreatePonderTags.DECORATION)
                .add(CDGBlocks.ANDESITE_GIRDER.getId())
                .add(CDGBlocks.SHEET_METAL_PANEL.getId());
    }
}
