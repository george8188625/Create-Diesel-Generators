package com.jesz.createdieselgenerators;

import net.neoforged.neoforge.common.ModConfigSpec;

public class CDGConfig {
    public static final ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec COMMON_SPEC;

    // Client configs:
    static {
        CLIENT_BUILDER.push("Client Configs");
    }

    public static final ModConfigSpec.ConfigValue<Boolean> FUEL_TOOLTIPS = CLIENT_BUILDER
            .comment("Fuel type tooltip on Buckets")
            .define("Fuel tooltips",true);

    public static final ModConfigSpec.ConfigValue<Boolean> ENGINES_EMIT_SOUND_ON_TRAINS = CLIENT_BUILDER
            .comment("Diesel Engines emit sounds on trains")
            .define("Diesel Engines emit sounds on trains",true);

    static {
        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();
    }

    // Server configs:
    static {
        SERVER_BUILDER.push("Server Configs");
        SERVER_BUILDER.push("Diesel Engines");
    }

    // Engines:
    public static final ModConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_MULTIPLIER = SERVER_BUILDER
            .comment("Turbocharged Diesel Engine Speed Multiplier")
            .define("Turbocharged Diesel Engine Speed Multiplier", 2d);

    public static final ModConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER = SERVER_BUILDER
            .comment("Turbocharged Diesel Engine Burn Rate Multiplier")
            .define("Turbocharged Diesel Engine Burn Rate Multiplier", 1d);

    public static final ModConfigSpec.ConfigValue<Boolean> NORMAL_ENGINES = SERVER_BUILDER
            .comment("Whether Normal Diesel Engines are enabled")
            .define("Normal Diesel Engines", true);

    public static final ModConfigSpec.ConfigValue<Boolean> MODULAR_ENGINES = SERVER_BUILDER
            .comment("Whether Modular Diesel Engines are enabled")
            .define("Modular Diesel Engines", true);

    public static final ModConfigSpec.ConfigValue<Boolean> HUGE_ENGINES = SERVER_BUILDER
            .comment("Whether Huge Diesel Engines are enabled")
            .define("Huge Diesel Engines", true);

    public static final ModConfigSpec.ConfigValue<Boolean> ENGINES_FILLED_WITH_ITEMS = SERVER_BUILDER
            .comment("Whether Diesel Engines can be filled with an Item")
            .define("Engines filled with a bucket", false);

    public static final ModConfigSpec.ConfigValue<Boolean> ENGINES_DISABLED_WITH_REDSTONE = SERVER_BUILDER
            .comment("Whether Diesel Engines can be disabled with redstone")
            .define("Engines disabled with redstone", true);

    public static final ModConfigSpec.ConfigValue<Boolean> ANALOG_SPEED_CONTROL = SERVER_BUILDER
            .comment("Whether Diesel Engines can be controlled with a analog lever.")
            .define("Engines controlled by analog lever", true);

    static {
        SERVER_BUILDER.pop();
        SERVER_BUILDER.push("Oil Config");
    }

    // Oil:

    public static final ModConfigSpec.ConfigValue<Integer> OIL_CHUNK_INFINITE_THRESHOLD = SERVER_BUILDER
            .comment()
            .define("Infinite oil chunk threshold", 10_000_000);

    public static final ModConfigSpec.ConfigValue<Integer> OIL_CHUNK_THRESHOLD = SERVER_BUILDER
            .comment()
            .define("Oil chunk threshold", 4_000_000);

    public static final ModConfigSpec.ConfigValue<Boolean> DISABLE_NORMAL_OIL_CHUNKS = SERVER_BUILDER
            .comment()
            .define("Disable normal oil chunks", false);

    public static final ModConfigSpec.ConfigValue<Boolean> DISABLE_HIGH_OIL_CHUNKS = SERVER_BUILDER
            .comment()
            .define("Disable high oil chunks", false);

    public static final ModConfigSpec.ConfigValue<Double> OIL_MULTIPLIER = SERVER_BUILDER
            .comment()
            .define("Normal oil chunks oil amount multiplier", 1.3d);

    public static final ModConfigSpec.ConfigValue<Double> HIGH_OIL_MULTIPLIER = SERVER_BUILDER
            .comment()
            .define("High oil chunks oil amount multiplier", 2d);

    public static final ModConfigSpec.ConfigValue<Double> OIL_CHUNK_SCALE = SERVER_BUILDER
            .comment("Size of the oil chunk map")
            .comment("Higher values make the map ")
            .define("Oil chunk map scale", 1.0d);

    public static final ModConfigSpec.ConfigValue<Integer> MAX_OIL_SCANNER_LEVEL = SERVER_BUILDER
            .comment("The maximum Y level at which oil scanners can be used")
            .define("Max Oil Scanner Level", 10000);

    static {
        SERVER_BUILDER.pop();
    }

    public static final ModConfigSpec.ConfigValue<Integer> MAX_OIL_BARREL_WIDTH = SERVER_BUILDER
            .comment("Maximum width of oil barrels")
            .define("Max Oil Barrel Width", 3);

    public static final ModConfigSpec.ConfigValue<Integer> MAX_OIL_BARREL_LENGTH_PER_WIDTH = SERVER_BUILDER
            .comment("Maximum oil barrel length for each unit of width")
            .define("Max Oil Barrel Length (per width)", 4);

    public static final ModConfigSpec.ConfigValue<Boolean> CANISTER_SPOUT_FILLING = SERVER_BUILDER
            .comment("Whether canisters can be filled by spouts")
            .define("Canister can be filled by spouts",true);

    public static final ModConfigSpec.ConfigValue<Boolean> COMBUSTIBLES_BLOW_UP = SERVER_BUILDER
            .comment("Whether fuels blow up when ignited")
            .define("Combustibles blow up",true);

    public static final ModConfigSpec.ConfigValue<Integer> DISTILLATION_MIN_HEIGHT = SERVER_BUILDER
            .comment("Minimum height of distillation tower to process recipes")
            .defineInRange("Distillation Tower Minimum Height", 3, 2, 7);

    static {
        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();
    }

    // Common configs:
    static {
        COMMON_BUILDER.push("Common Config");
    }

    public static final ModConfigSpec.ConfigValue<Integer> TOOL_CAPACITY = COMMON_BUILDER
            .comment("Capacity of Tools requiring Fluids in mB")
            .define("Capacity of Tools requiring Fluids", 200);

    public static final ModConfigSpec.ConfigValue<Integer> TOOL_CAPACITY_ENCHANTMENT = COMMON_BUILDER
            .comment("Tool Capacity Enchantment Capacity Addition in mB")
            .define("Capacity Addition of Tools with Capacity Enchantment", 100);

    public static final ModConfigSpec.ConfigValue<Integer> CANISTER_CAPACITY = COMMON_BUILDER
            .comment("Canister Capacity in mB")
            .define("Capacity of Canisters", 4000);

    public static final ModConfigSpec.ConfigValue<Integer> CANISTER_CAPACITY_ENCHANTMENT = COMMON_BUILDER
            .comment("Canister Capacity Enchantment Capacity Addition in mB")
            .define("Capacity Addition of Capacity Enchantment in Canisters", 1000);

    static {
        COMMON_BUILDER.pop();
        COMMON_SPEC = COMMON_BUILDER.build();
    }
}
