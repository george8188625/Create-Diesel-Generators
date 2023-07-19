package com.jesz.createdieselgenerators.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigRegistry {
    public static final ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final ForgeConfigSpec.ConfigValue<Double> SLOW_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Double> FAST_SPEED;
    public static final ForgeConfigSpec.ConfigValue<Double> WEAK_STRESS;
    public static final ForgeConfigSpec.ConfigValue<Double> STRONG_STRESS;

    public static final ForgeConfigSpec.ConfigValue<Boolean> FUEL_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> ETHANOL_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PLANTOIL_TAG;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BIODIESEL_TAG;


    static {
        SERVER_BUILDER.push("Server Configs");

        FUEL_TAG = SERVER_BUILDER.comment("forge:fuel tag compatibility")
                .define("forge:fuel tag compatibility", true);
        ETHANOL_TAG = SERVER_BUILDER.comment("forge:ethanol tag compatibility")
                .define("forge:ethanol tag compatibility", true);
        PLANTOIL_TAG = SERVER_BUILDER.comment("forge:plantoil tag compatibility")
                .define("forge:plantoil tag compatibility", true);
        BIODIESEL_TAG = SERVER_BUILDER.comment("forge:biodiesel tag compatibility")
                .define("forge:biodiesel tag compatibility", true);


        SLOW_SPEED = SERVER_BUILDER.comment("Speed of Slow Fuel Type in RPM")
                .define("Speed of Slow Fuel Type",48d);

        FAST_SPEED = SERVER_BUILDER.comment("Speed of Fast Fuel Type in RPM")
                .define("Speed of Fast Fuel Type",96d);

        WEAK_STRESS = SERVER_BUILDER.comment("Strength of Weak Fuel Type in su")
                .define("Strength of Weak Fuel Type",512d);

        STRONG_STRESS = SERVER_BUILDER.comment("Strength of Strong Fuel Type in su")
                        .define("Strength of Strong Fuel Type",1024d);


        SERVER_BUILDER.pop();
        SERVER_SPEC = SERVER_BUILDER.build();
    }
}
