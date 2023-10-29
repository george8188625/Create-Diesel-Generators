package com.jesz.createdieselgenerators.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigRegistry {
    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec CLIENT_SPEC;


    public static final ForgeConfigSpec.ConfigValue<Double> SLOW_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Double> FAST_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Double> WEAK_STRESS;
    public static final ForgeConfigSpec.ConfigValue<Double> STRONG_STRESS;

    public static final ForgeConfigSpec.ConfigValue<Double> MODULAR_ENGINE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> HUGE_ENGINE_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Boolean> DISTILLATION_WIDE_TANK_FASTER;
    public static final ForgeConfigSpec.ConfigValue<Integer> DISTILLATION_LEVEL_HEIGHT;

    public static final ForgeConfigSpec.ConfigValue<Boolean> CANISTER_SPOUT_FILLING;
    public static final ForgeConfigSpec.ConfigValue<Integer> CANISTER_CAPACITY;
    public static final ForgeConfigSpec.ConfigValue<Integer> FAST_BURN_RATE;
    public static final ForgeConfigSpec.ConfigValue<Integer> SLOW_BURN_RATE;

    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_OIL_SCANNER_LEVEL;

    public static final ForgeConfigSpec.ConfigValue<Double> OIL_MULTIPLIER;
    public static final ForgeConfigSpec.ConfigValue<Double> HIGH_OIL_MULTIPLIER;

    public static final ForgeConfigSpec.ConfigValue<Double> OIL_PERCENTAGE;
    public static final ForgeConfigSpec.ConfigValue<Double> HIGH_OIL_PERCENTAGE;

    public static final ForgeConfigSpec.ConfigValue<Boolean> FUEL_TOOLTIPS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DIESEL_ENGINE_IN_JEI;

    public static final ForgeConfigSpec.ConfigValue<Boolean> COMBUSTIBLES_BLOW_UP;

    public static final ForgeConfigSpec.ConfigValue<Boolean> FUEL_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ETHANOL_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PLANTOIL_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BIODIESEL_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> GASOLINE_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> DIESEL_TAG;


    static {

        CLIENT_BUILDER.push("Client Configs");

        FUEL_TOOLTIPS = CLIENT_BUILDER.comment("Fuel type tooltip on Buckets")
            .define("Fuel tooltips",true);
        DIESEL_ENGINE_IN_JEI = CLIENT_BUILDER.comment("Whenever Diesel Engines display in JEI")
                .define("Diesel Engine JEI Config",true);

        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();

        SERVER_BUILDER.push("Server Configs");
        SERVER_BUILDER.push("Diesel Engines");

            SERVER_BUILDER.push("Tag Compatibility");
                FUEL_TAG = SERVER_BUILDER.comment("forge:fuel tag compatibility")
                        .define("forge:fuel tag compatibility", true);
                ETHANOL_TAG = SERVER_BUILDER.comment("forge:ethanol tag compatibility")
                        .define("forge:ethanol tag compatibility", true);
                PLANTOIL_TAG = SERVER_BUILDER.comment("forge:plantoil tag compatibility")
                        .define("forge:plantoil tag compatibility", true);
                BIODIESEL_TAG = SERVER_BUILDER.comment("forge:biodiesel tag compatibility")
                        .define("forge:biodiesel tag compatibility", true);
                DIESEL_TAG = SERVER_BUILDER.comment("forge:diesel tag compatibility")
                        .define("forge:diesel tag compatibility", true);
                GASOLINE_TAG = SERVER_BUILDER.comment("forge:gasoline tag compatibility")
                        .define("forge:gasoline tag compatibility", true);
            SERVER_BUILDER.pop();

            SERVER_BUILDER.push("Stress/Speed/Burn Rate Values");
                SERVER_BUILDER.push("Engine Type Multipliers");
                    MODULAR_ENGINE_MULTIPLIER = SERVER_BUILDER.comment("Modular Diesel Engine Generated Stress Multiplier")
                            .define("Modular Diesel Engine Stress Multiplier", 1.25d);
                    HUGE_ENGINE_MULTIPLIER = SERVER_BUILDER.comment("Huge Diesel Engine Generated Stress Multiplier")
                            .define("Huge Diesel Engine Stress Multiplier", 1.75d);
                    TURBOCHARGED_ENGINE_MULTIPLIER = SERVER_BUILDER.comment("Turbocharged Diesel Engine Speed Multiplier")
                            .define("Turbocharged Diesel Engine Multiplier", 2d);
                    TURBOCHARGED_ENGINE_BURN_RATE_MULTIPLIER = SERVER_BUILDER.comment("Turbocharged Diesel Engine Speed Multiplier")
                            .define("Turbocharged Diesel Engine Multiplier", 1d);
                SERVER_BUILDER.pop();

                FAST_BURN_RATE = SERVER_BUILDER.comment("Diesel Engine Fuel fast burn rate per second")
                        .define("Diesel Engine Fuel fast burn rate", 4);
                SLOW_BURN_RATE = SERVER_BUILDER.comment("Diesel Engine Fuel slow burn rate per second")
                        .define("Diesel Engine Fuel slow burn rate", 2);
                SLOW_SPEED = SERVER_BUILDER.comment("Speed of Slow Fuel Type in RPM")
                        .define("Speed of Slow Fuel Type",48d);
                FAST_SPEED = SERVER_BUILDER.comment("Speed of Fast Fuel Type in RPM")
                        .define("Speed of Fast Fuel Type",96d);
                WEAK_STRESS = SERVER_BUILDER.comment("Strength of Weak Fuel Type in su")
                        .define("Strength of Weak Fuel Type",1024d);
                STRONG_STRESS = SERVER_BUILDER.comment("Strength of Strong Fuel Type in su")
                        .define("Strength of Strong Fuel Type",2048d);
            SERVER_BUILDER.pop();

        SERVER_BUILDER.pop();

        SERVER_BUILDER.push("Oil Config");
            OIL_MULTIPLIER = SERVER_BUILDER.comment("Normal oil chunks oil amount multiplier")
                    .define("Normal oil chunks oil amount multiplier", 1d);
            HIGH_OIL_MULTIPLIER = SERVER_BUILDER.comment("High oil chunks oil amount multiplier")
                    .define("High oil chunks oil amount multiplier", 1d);
            MAX_OIL_SCANNER_LEVEL = SERVER_BUILDER.comment("Max Oil Scanner Level")
                .define("Max Oil Scanner Level", 10000);
            OIL_PERCENTAGE = SERVER_BUILDER.comment("Normal oil chunks percentage")
                    .defineInRange("Normal oil chunks percentage", 5d, 0d, 100d);
            HIGH_OIL_PERCENTAGE = SERVER_BUILDER.comment("High oil chunks percentage")
                    .defineInRange("High oil chunks percentage", 5d, 0d, 100d);
            SERVER_BUILDER.push("Distillation");
                DISTILLATION_WIDE_TANK_FASTER = SERVER_BUILDER.comment("Whenever wide Distillation Towers go faster than the thin ones")
                        .define("Wide Distillation Tower Distill Faster", true);
                DISTILLATION_LEVEL_HEIGHT = SERVER_BUILDER.comment("Height of Distillation Tower level")
                        .defineInRange("Height of Distillation Tower level", 1, 1, 3);
            SERVER_BUILDER.pop();

        SERVER_BUILDER.pop();



        CANISTER_CAPACITY = SERVER_BUILDER.comment("Canister Capacity in mB")
                .define("Capacity of Canisters",4000);

        CANISTER_SPOUT_FILLING = SERVER_BUILDER.comment("Canister can be filled by spouts")
                .define("Canister can be filled by spouts",true);

        COMBUSTIBLES_BLOW_UP = SERVER_BUILDER.comment("Combustibles do boom boom when on fire")
                .define("Combustibles blow up",true);

        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();
    }


}
