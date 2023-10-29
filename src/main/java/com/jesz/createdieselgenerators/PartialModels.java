package com.jesz.createdieselgenerators;

import com.jozufozu.flywheel.core.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class PartialModels {
    public static final PartialModel ENGINE_PISTON            = new PartialModel(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/piston"));
    public static final PartialModel ENGINE_PISTON_LINKAGE    = new PartialModel(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/linkage"));
    public static final PartialModel ENGINE_PISTON_CONNECTOR  = new PartialModel(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/shaft_connector"));

    public static final PartialModel PUMPJACK_ROPE            = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_rope"));
    public static final PartialModel PUMPJACK_CRANK_SMALL     = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/small_counterweight"));
    public static final PartialModel PUMPJACK_CRANK_ROD_SMALL = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/small_rod"));
    public static final PartialModel PUMPJACK_CRANK_LARGE     = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/large_counterweight"));
    public static final PartialModel PUMPJACK_CRANK_ROD_LARGE = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/large_rod"));
    public static final PartialModel SMALL_GAUGE_DIAL         = new PartialModel(new ResourceLocation("createdieselgenerators:block/basin_lid/gauge_dial"));
    public static final PartialModel DISTILLATION_GAUGE       = new PartialModel(new ResourceLocation("createdieselgenerators:block/distillation_tower/gauge"));
    public static final PartialModel DISTILLATION_GAUGE_DIAL  = new PartialModel(new ResourceLocation("createdieselgenerators:block/distillation_tower/gauge_dial"));

    public static void Init(){

    }
}
