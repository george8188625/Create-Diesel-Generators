package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.other.CDGPartialModel;
import com.jozufozu.flywheel.core.PartialModel;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class PartialModels {
    public static final PartialModel MODULAR_ENGINE_PISTONS_0  = new PartialModel(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_0"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_1  = new PartialModel(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_1"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_2  = new PartialModel(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_2"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_3  = new PartialModel(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_3"));
    public static final PartialModel MODULAR_ENGINE_PISTONS_4  = new PartialModel(new ResourceLocation("createdieselgenerators:block/modular_diesel_engine/pistons/pistons_4"));
    public static final PartialModel ENGINE_PISTONS_0          = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_0"));
    public static final PartialModel ENGINE_PISTONS_1          = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_1"));
    public static final PartialModel ENGINE_PISTONS_2          = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_2"));
    public static final PartialModel ENGINE_PISTONS_3          = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_3"));
    public static final PartialModel ENGINE_PISTONS_4          = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/pistons_4"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_0 = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_0"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_1 = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_1"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_2 = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_2"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_3 = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_3"));
    public static final PartialModel ENGINE_PISTONS_VERTICAL_4 = new PartialModel(new ResourceLocation("createdieselgenerators:block/diesel_engine/pistons/vertical_4"));

    public static final PartialModel ENGINE_PISTON             = new PartialModel(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/piston"));
    public static final PartialModel ENGINE_PISTON_LINKAGE     = new PartialModel(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/linkage"));
    public static final PartialModel ENGINE_PISTON_CONNECTOR   = new PartialModel(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/shaft_connector"));

    public static final PartialModel PUMPJACK_ROPE             = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_rope"));
    public static final PartialModel PUMPJACK_CRANK_SMALL      = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/small_counterweight"));
    public static final PartialModel PUMPJACK_CRANK_ROD_SMALL  = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/small_rod"));
    public static final PartialModel PUMPJACK_CRANK_LARGE      = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/large_counterweight"));
    public static final PartialModel PUMPJACK_CRANK_ROD_LARGE  = new PartialModel(new ResourceLocation("createdieselgenerators:block/pumpjack_crank/large_rod"));
    public static final PartialModel SMALL_GAUGE_DIAL          = new PartialModel(new ResourceLocation("createdieselgenerators:block/basin_lid/gauge_dial"));
    public static final PartialModel DISTILLATION_GAUGE        = new PartialModel(new ResourceLocation("createdieselgenerators:block/distillation_tower/gauge"));
    public static final PartialModel DISTILLATION_GAUGE_DIAL   = new PartialModel(new ResourceLocation("createdieselgenerators:block/distillation_tower/gauge_dial"));
    public static final PartialModel JEI_DISTILLER_TOP         = new PartialModel(new ResourceLocation("createdieselgenerators:block/jei_distiller/top"));
    public static final PartialModel JEI_DISTILLER_MIDDLE      = new PartialModel(new ResourceLocation("createdieselgenerators:block/jei_distiller/middle"));
    public static final PartialModel JEI_DISTILLER_BOTTOM      = new PartialModel(new ResourceLocation("createdieselgenerators:block/jei_distiller/bottom"));
    public static final PartialModel JEI_ENGINE_PISTON         = new PartialModel(new ResourceLocation("createdieselgenerators:block/huge_diesel_engine/jei_piston"));
    public static Map<String, Pair<CDGPartialModel, Pair<CDGPartialModel, CDGPartialModel>>> lighterSkinModels = new HashMap<>();
    public static void init(){}
    public static void initSkins(){
        lighterSkinModels.clear();
        lighterSkinModels.put("standard", Pair.of(new CDGPartialModel(new ResourceLocation("createdieselgenerators:item/lighter"))
                , Pair.of(new CDGPartialModel(new ResourceLocation("createdieselgenerators:item/lighter_open"))
                        , new CDGPartialModel(new ResourceLocation("createdieselgenerators:item/lighter_ignited")))));
        CreateDieselGenerators.lighterSkins.forEach((name, skinId) -> {
            lighterSkinModels.put(skinId, Pair.of(new CDGPartialModel(new ResourceLocation("createdieselgenerators:item/lighter/"+skinId))
                    , Pair.of(new CDGPartialModel(new ResourceLocation("createdieselgenerators:item/lighter/"+skinId+"_open"))
                    , new CDGPartialModel(new ResourceLocation("createdieselgenerators:item/lighter/"+skinId+"_ignited")))));
        });
    }
}

