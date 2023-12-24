package com.jesz.createdieselgenerators.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigRegistry {
    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> DISTILLATION_WIDE_TANK_FASTER;
    public static final ForgeConfigSpec.ConfigValue<Integer> DISTILLATION_LEVEL_HEIGHT;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENGINES_EMIT_SOUND_ON_TRAINS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> CANISTER_SPOUT_FILLING;
    public static final ForgeConfigSpec.ConfigValue<Integer> CANISTER_CAPACITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> CANISTER_CAPACITY_ENCHANTMENT;

    public static final ForgeConfigSpec.ConfigValue<Integer> TOOL_CAPACITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> TOOL_CAPACITY_ENCHANTMENT;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_OIL_SCANNER_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_OIL_BARREL_WIDTH;

    public static final ForgeConfigSpec.ConfigValue<Boolean> OIL_DEPOSITS_INFINITE;

    public static final ForgeConfigSpec.ConfigValue<Double> OIL_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> HIGH_OIL_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Double> OIL_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Double> HIGH_OIL_PERCENTAGE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> FUEL_TOOLTIPS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DIESEL_ENGINE_IN_JEI;

    public static final ForgeConfigSpec.ConfigValue<Boolean> COMBUSTIBLES_BLOW_UP;

    public static final ForgeConfigSpec.ConfigValue<Boolean> NORMAL_ENGINES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MODULAR_ENGINES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HUGE_ENGINES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENGINES_FILLED_WITH_ITEMS;

    static {

        CLIENT_BUILDER.push("Client Configs");

        FUEL_TOOLTIPS = CLIENT_BUILDER.comment("Fuel type tooltip on Buckets")
            .define("Fuel tooltips",true);
        DIESEL_ENGINE_IN_JEI = CLIENT_BUILDER.comment("Whenever Diesel Engines display in JEI")
                .define("Diesel Engine JEI Config",true);
        ENGINES_EMIT_SOUND_ON_TRAINS = CLIENT_BUILDER.comment("Diesel Engines emit sounds on trains")
                .define("Diesel Engines emit sounds on trains",true);
        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();

        SERVER_BUILDER.push("Server Configs");
        SERVER_BUILDER.push("Diesel Engines");

            TURBOCHARGED_ENGINE_MULTIPLIER = SERVER_BUILDER.comment("Turbocharged Diesel Engine Speed Multiplier")
                    .define("Turbocharged Diesel Engine Speed Multiplier", 2d);
            TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER = SERVER_BUILDER.comment("Turbocharged Diesel Engine Burn Rate Multiplier")
                    .define("Turbocharged Diesel Engine Burn Rate Multiplier", 1d);

            NORMAL_ENGINES = SERVER_BUILDER.comment("Whenever Normal Diesel Engines are enabled")
                    .define("Normal Diesel Engines", true);
            MODULAR_ENGINES = SERVER_BUILDER.comment("Whenever Modular Diesel Engines are enabled")
                    .define("Modular Diesel Engines", true);
            HUGE_ENGINES = SERVER_BUILDER.comment("Whenever Huge Diesel Engines are enabled")
                    .define("Huge Diesel Engines", true);

            ENGINES_FILLED_WITH_ITEMS = SERVER_BUILDER.comment("Whenever Diesel Engines can be filled with an Item")
                    .define("Engines can be filled with a bucket", false);

        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Oil Config");
            OIL_DEPOSITS_INFINITE = SERVER_BUILDER.comment("Whenever crude oil deposits are infinite")
                    .define("Infinite oil deposits", false);
            OIL_MULTIPLIER = SERVER_BUILDER.comment("Normal oil chunks oil amount multiplier")
                    .define("Normal oil chunks oil amount multiplier", 1d);
            HIGH_OIL_MULTIPLIER = SERVER_BUILDER.comment("High oil chunks oil amount multiplier")
                    .define("High oil chunks oil amount multiplier", 1d);
            MAX_OIL_SCANNER_LEVEL = SERVER_BUILDER.comment("Max Oil Scanner Level")
                .define("Max Oil Scanner Level", 10000);
            OIL_PERCENTAGE = SERVER_BUILDER.comment("Normal oil chunks percentage")
                    .defineInRange("Normal oil chunks percentage", 10d, 0d, 100d);
            HIGH_OIL_PERCENTAGE = SERVER_BUILDER.comment("High oil chunks percentage")
                    .defineInRange("High oil chunks percentage", 10d, 0d, 100d);
            SERVER_BUILDER.push("Distillation");
                DISTILLATION_WIDE_TANK_FASTER = SERVER_BUILDER.comment("Whenever wide Distillation Towers go faster than the thin ones")
                        .define("Wide Distillation Tower Distill Faster", true);
                DISTILLATION_LEVEL_HEIGHT = SERVER_BUILDER.comment("Height of Distillation Tower level")
                        .defineInRange("Height of Distillation Tower level", 1, 1, 3);
            SERVER_BUILDER.pop();

        SERVER_BUILDER.pop();


        MAX_OIL_BARREL_WIDTH = SERVER_BUILDER.comment("Maximum width of Oil Barrels")
                .define("Max Oil Barrel Width", 3);
        
        CANISTER_CAPACITY = SERVER_BUILDER.comment("Canister Capacity in mB")
                .define("Capacity of Canisters",4000);
        CANISTER_CAPACITY_ENCHANTMENT = SERVER_BUILDER.comment("Canister Capacity Enchantment Capacity Addition in mB")
                .define("Capacity Addition of Capacity Enchantment in Canisters",1000);
        CANISTER_SPOUT_FILLING = SERVER_BUILDER.comment("Canister can be filled by spouts")
                .define("Canister can be filled by spouts",true);

        TOOL_CAPACITY = SERVER_BUILDER.comment("Capacity of Tools requiring Fluids in mB")
                .define("Capacity of Tools requiring Fluids",200);
        TOOL_CAPACITY_ENCHANTMENT = SERVER_BUILDER.comment("Tool Capacity Enchantment Capacity Addition in mB")
                .define("Capacity Addition of Tools with Capacity Enchantment",10);

        COMBUSTIBLES_BLOW_UP = SERVER_BUILDER.comment("Combustibles do boom boom when on fire")
                .define("Combustibles blow up",true);

        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();
    }


}
