package com.jesz.createdieselgenerators.blocks.ct;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import net.minecraft.resources.ResourceLocation;

public class SpriteShifts {
    public static final CTSpriteShiftEntry MODULAR_DIESEL_ENGINE = horizontal("diesel_engine_big");
    public static final CTSpriteShiftEntry DISTILLATION_TANK = rectangle("distillation_tower/distillation_tank"),
            DISTILLATION_TANK_TOP = rectangle("distillation_tower/distillation_tank_top"),
            DISTILLATION_TANK_NORTH = rectangle("distillation_tower/distillation_tank", "distillation_tower/distillation_tank_pipes_connected");

    public static final CTSpriteShiftEntry OIL_BARREL_TOP = rectangle("oil_barrel/top"),
            OIL_BARREL_SIDE = rectangle("oil_barrel/sideways/iron"),
            OIL_BARREL_SIDE_WHITE = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/white_connected"),
            OIL_BARREL_SIDE_ORANGE = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/orange_connected"),
            OIL_BARREL_SIDE_MAGENTA = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/magenta_connected"),
            OIL_BARREL_SIDE_LIGHT_BLUE = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/light_blue_connected"),
            OIL_BARREL_SIDE_YELLOW = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/yellow_connected"),
            OIL_BARREL_SIDE_LIME = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/lime_connected"),
            OIL_BARREL_SIDE_PINK = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/pink_connected"),
            OIL_BARREL_SIDE_GRAY = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/gray_connected"),
            OIL_BARREL_SIDE_LIGHT_GRAY = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/light_gray_connected"),
            OIL_BARREL_SIDE_CYAN = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/cyan_connected"),
            OIL_BARREL_SIDE_PURPLE = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/purple_connected"),
            OIL_BARREL_SIDE_BLUE = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/blue_connected"),
            OIL_BARREL_SIDE_BROWN = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/brown_connected"),
            OIL_BARREL_SIDE_GREEN = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/green_connected"),
            OIL_BARREL_SIDE_RED = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/red_connected"),
            OIL_BARREL_SIDE_BLACK = rectangle("oil_barrel/sideways/iron", "oil_barrel/sideways/black_connected"),
            OIL_BARREL = rectangle("oil_barrel/vertical/iron"),
            OIL_BARREL_WHITE = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/white_connected"),
            OIL_BARREL_ORANGE = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/orange_connected"),
            OIL_BARREL_MAGENTA = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/magenta_connected"),
            OIL_BARREL_LIGHT_BLUE = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/light_blue_connected"),
            OIL_BARREL_YELLOW = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/yellow_connected"),
            OIL_BARREL_LIME = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/lime_connected"),
            OIL_BARREL_PINK = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/pink_connected"),
            OIL_BARREL_GRAY = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/gray_connected"),
            OIL_BARREL_LIGHT_GRAY = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/light_gray_connected"),
            OIL_BARREL_CYAN = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/cyan_connected"),
            OIL_BARREL_PURPLE = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/purple_connected"),
            OIL_BARREL_BLUE = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/blue_connected"),
            OIL_BARREL_BROWN = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/brown_connected"),
            OIL_BARREL_GREEN = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/green_connected"),
            OIL_BARREL_RED = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/red_connected"),
            OIL_BARREL_BLACK = rectangle("oil_barrel/vertical/iron", "oil_barrel/vertical/black_connected");
    public static void init(){}
    private static CTSpriteShiftEntry horizontal(String name) {
        return CTSpriteShifter.getCT(AllCTTypes.CROSS, new ResourceLocation("createdieselgenerators:block/"+name),
                new ResourceLocation("createdieselgenerators:block/"+name+"_connected"));
    }
    private static CTSpriteShiftEntry rectangle(String name) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, new ResourceLocation("createdieselgenerators:block/"+name),
                new ResourceLocation("createdieselgenerators:block/"+name+"_connected"));
    }

    private static CTSpriteShiftEntry rectangle(String name, String connectedName) {
        return CTSpriteShifter.getCT(AllCTTypes.RECTANGLE, new ResourceLocation("createdieselgenerators:block/"+name),
                new ResourceLocation("createdieselgenerators:block/"+connectedName));
    }

}
