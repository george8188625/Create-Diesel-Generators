package com.jesz.createdieselgenerators.fluids;

import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class FluidRegistry {

    public static final FluidEntry<ForgeFlowingFluid.Flowing> PLANT_OIL =
            REGISTRATE.fluid("plant_oil", new ResourceLocation("createdieselgenerators:fluid/plant_oil_still"), new ResourceLocation("createdieselgenerators:fluid/plant_oil_flow"))
                    .lang("Plant Oil")
                    .attributes(b -> b.viscosity(1500)
                            .density(500))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> CRUDE_OIL =
            REGISTRATE.fluid("crude_oil", new ResourceLocation("createdieselgenerators:fluid/crude_oil_still"), new ResourceLocation("createdieselgenerators:fluid/crude_oil_flow"))
                    .lang("Crude Oil")
                    .attributes(b -> b.viscosity(1500)
                            .density(100))
                    .properties(p -> p.levelDecreasePerBlock(3)
                            .tickRate(25)
                            .slopeFindDistance(2)
                            .explosionResistance(100f))
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> BIODIESEL =
            REGISTRATE.fluid("biodiesel", new ResourceLocation("createdieselgenerators:fluid/biodiesel_still"), new ResourceLocation("createdieselgenerators:fluid/biodiesel_flow"))
                    .lang("Biodiesel")
                    .attributes(b -> b.viscosity(1500)
                            .density(500))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> DIESEL =
            REGISTRATE.fluid("diesel", new ResourceLocation("createdieselgenerators:fluid/diesel_still"), new ResourceLocation("createdieselgenerators:fluid/diesel_flow"))
                    .lang("Diesel")
                    .attributes(b -> b.viscosity(1500)
                            .density(500))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> GASOLINE =
            REGISTRATE.fluid("gasoline", new ResourceLocation("createdieselgenerators:fluid/gasoline_still"), new ResourceLocation("createdieselgenerators:fluid/gasoline_flow"))
                    .lang("Gasoline")
                    .attributes(b -> b.viscosity(1500)
                            .density(500))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ETHANOL =
            REGISTRATE.fluid("ethanol", new ResourceLocation("createdieselgenerators:fluid/ethanol_still"), new ResourceLocation("createdieselgenerators:fluid/ethanol_flow"))
                    .lang("Ethanol")
                    .attributes(b -> b.viscosity(1500)
                            .density(500))
                    .properties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(5)
                            .explosionResistance(100f))
                    .register();

    public static void register() {}


}
