package com.jesz.createdieselgenerators;

import net.minecraftforge.common.ForgeConfigSpec;

public class CDGConfig {
    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> ENGINES_EMIT_SOUND_ON_TRAINS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> CANISTER_SPOUT_FILLING;
    public static final ForgeConfigSpec.ConfigValue<Integer> CANISTER_CAPACITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> CANISTER_CAPACITY_ENCHANTMENT;

    public static final ForgeConfigSpec.ConfigValue<Integer> TOOL_CAPACITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> TOOL_CAPACITY_ENCHANTMENT;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_OIL_SCANNER_LEVEL;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_OIL_BARREL_WIDTH;

    public static final ForgeConfigSpec.ConfigValue<Integer> OIL_CHUNK_INFINITE_THRESHOLD;
    public static final ForgeConfigSpec.ConfigValue<Integer> OIL_CHUNK_THRESHOLD;

    public static final ForgeConfigSpec.ConfigValue<Boolean> DISABLE_NORMAL_OIL_CHUNKS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DISABLE_HIGH_OIL_CHUNKS;

    public static final ForgeConfigSpec.ConfigValue<Double> OIL_CHUNK_SCALE;
    public static final ForgeConfigSpec.ConfigValue<Double> OIL_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> HIGH_OIL_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> FUEL_TOOLTIPS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DIESEL_ENGINE_IN_JEI;

    public static final ForgeConfigSpec.ConfigValue<Boolean> COMBUSTIBLES_BLOW_UP;

    public static final ForgeConfigSpec.ConfigValue<Boolean> NORMAL_ENGINES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> MODULAR_ENGINES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> HUGE_ENGINES;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENGINES_FILLED_WITH_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ENGINES_DISABLED_WITH_REDSTONE;

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
                    .define("Engines filled with a bucket", false);
            ENGINES_DISABLED_WITH_REDSTONE = SERVER_BUILDER.comment("Whenever Diesel Engines can be disabled with redstone")
                    .define("Engines disabled with redstone", true);

        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Oil Config");
            OIL_CHUNK_INFINITE_THRESHOLD = SERVER_BUILDER.comment()
                    .define("Infinite oil chunk threshold", 10_000_000);
            OIL_CHUNK_THRESHOLD = SERVER_BUILDER.comment()
                    .define("Oil chunk threshold", 4_000_000);
            DISABLE_NORMAL_OIL_CHUNKS = SERVER_BUILDER.comment()
                    .define("Disable normal oil chunks", false);
            DISABLE_HIGH_OIL_CHUNKS = SERVER_BUILDER.comment()
                    .define("Disable high oil chunks", false);
            OIL_MULTIPLIER = SERVER_BUILDER.comment()
                    .define("Normal oil chunks oil amount multiplier", 1.3d);
            HIGH_OIL_MULTIPLIER = SERVER_BUILDER.comment()
                    .define("High oil chunks oil amount multiplier", 2d);
            OIL_CHUNK_SCALE = SERVER_BUILDER.comment()
                    .define("Oil chunk map scale", 1.0d);
            MAX_OIL_SCANNER_LEVEL = SERVER_BUILDER.comment()
                .define("Max Oil Scanner Level", 10000);

        SERVER_BUILDER.pop();

        MAX_OIL_BARREL_WIDTH = SERVER_BUILDER.comment("Maximum width of Oil Barrels")
                .define("Max Oil Barrel Width", 3);
        


        CANISTER_SPOUT_FILLING = SERVER_BUILDER.comment("Canister can be filled by spouts")
                .define("Canister can be filled by spouts",true);

        COMBUSTIBLES_BLOW_UP = SERVER_BUILDER.comment("Combustibles do boom boom when on fire")
                .define("Combustibles blow up",true);

        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();

        COMMON_BUILDER.push("Common Config");
            TOOL_CAPACITY = COMMON_BUILDER.comment("Capacity of Tools requiring Fluids in mB")
                    .define("Capacity of Tools requiring Fluids",200);
            TOOL_CAPACITY_ENCHANTMENT = COMMON_BUILDER.comment("Tool Capacity Enchantment Capacity Addition in mB")
                    .define("Capacity Addition of Tools with Capacity Enchantment",100);
        CANISTER_CAPACITY = COMMON_BUILDER.comment("Canister Capacity in mB")
                .define("Capacity of Canisters",4000);
        CANISTER_CAPACITY_ENCHANTMENT = COMMON_BUILDER.comment("Canister Capacity Enchantment Capacity Addition in mB")
                .define("Capacity Addition of Capacity Enchantment in Canisters",1000);

        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();

    }


}
