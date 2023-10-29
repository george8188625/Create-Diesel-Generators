package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.infrastructure.ponder.AllPonderTags;

public class PonderIndex {
    static final PonderRegistrationHelper HELPER = new PonderRegistrationHelper("createdieselgenerators");
    public static void register() {
        HELPER.forComponents(ItemRegistry.DISTILLATION_CONTROLLER)
                .addStoryBoard("distillation_tower", DistillationScenes::distillation);
        HELPER.forComponents(BlockRegistry.DIESEL_ENGINE)
                .addStoryBoard("diesel_engine", DieselEngineScenes::small);
        HELPER.forComponents(BlockRegistry.DIESEL_ENGINE)
                .addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
        HELPER.forComponents(BlockRegistry.MODULAR_DIESEL_ENGINE)
                .addStoryBoard("large_diesel_engine", DieselEngineScenes::modular);
        HELPER.forComponents(BlockRegistry.MODULAR_DIESEL_ENGINE)
                .addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
        HELPER.forComponents(ItemRegistry.ENGINE_SILENCER)
                .addStoryBoard("engine_silencer", DieselEngineScenes::silencer);
        HELPER.forComponents(BlockRegistry.BASIN_LID)
                .addStoryBoard("basin_fermenting_station", BasinScenes::basin_lid);
        HELPER.forComponents(BlockRegistry.HUGE_DIESEL_ENGINE)
                .addStoryBoard("huge_diesel_engine", DieselEngineScenes::huge);
        HELPER.forComponents(BlockRegistry.PUMPJACK_BEARING, BlockRegistry.PUMPJACK_CRANK, BlockRegistry.PUMPJACK_HEAD)
                .addStoryBoard("pumpjack", OilScenes::pumpjack);

        PonderRegistry.TAGS.forTag(AllPonderTags.KINETIC_SOURCES)
                .add(BlockRegistry.DIESEL_ENGINE)
                .add(BlockRegistry.MODULAR_DIESEL_ENGINE)
                .add(BlockRegistry.HUGE_DIESEL_ENGINE);
        PonderRegistry.TAGS.forTag(AllPonderTags.KINETIC_APPLIANCES)
                .add(BlockRegistry.BASIN_LID);
        PonderRegistry.TAGS.forTag(AllPonderTags.DISPLAY_SOURCES)
                .add(BlockRegistry.DIESEL_ENGINE)
                .add(BlockRegistry.MODULAR_DIESEL_ENGINE);
    }
}
