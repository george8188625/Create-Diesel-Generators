package com.jesz.createdieselgenerators.fluids;

import com.simibubi.create.AllFluids;
import com.simibubi.create.foundation.utility.Color;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.joml.Vector3f;

import java.util.function.Supplier;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class FluidRegistry {

    public static final FluidEntry<ForgeFlowingFluid.Flowing> PLANT_OIL =
            REGISTRATE.fluid("plant_oil", new ResourceLocation("createdieselgenerators:block/plant_oil_still"), new ResourceLocation("createdieselgenerators:block/plant_oil_flow"))
                    .lang("Plant Oil")
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> BIODIESEL =
            REGISTRATE.fluid("biodiesel", new ResourceLocation("createdieselgenerators:block/biodiesel_still"), new ResourceLocation("createdieselgenerators:block/biodiesel_flow"))
                    .lang("Biodiesel")
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .register();
    public static final FluidEntry<ForgeFlowingFluid.Flowing> ETHANOL =
            REGISTRATE.fluid("ethanol", new ResourceLocation("createdieselgenerators:block/ethanol_still"), new ResourceLocation("createdieselgenerators:block/ethanol_flow"))
                    .lang("Ethanol")
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(5)
                            .explosionResistance(100f))
                    .register();

    public static void register() {}

}
