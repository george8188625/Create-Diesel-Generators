package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import mezz.jei.api.recipe.RecipeType;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class DieselEngineJeiRecipeType {
    public static final RecipeType<DieselEngineJeiRecipeType> DIESEL_BURNING =
            RecipeType.create("createdieselgenerators", "diesel_burning", DieselEngineJeiRecipeType.class);

    int burnRate;
    float speed;
    List<FluidStack> fluids;
    float stress;
    public DieselEngineJeiRecipeType(String type, List<FluidStack> fluids) {
        this.stress = type.charAt(1) == 's' ? ConfigRegistry.STRONG_STRESS.get().floatValue() : ConfigRegistry.WEAK_STRESS.get().floatValue();
        this.speed = type.charAt(0) == 'f' ? ConfigRegistry.FAST_SPEED.get().floatValue() : ConfigRegistry.SLOW_SPEED.get().floatValue();
        this.burnRate = type.charAt(2) == 'f' ? ConfigRegistry.FAST_BURN_RATE.get() : ConfigRegistry.SLOW_BURN_RATE.get();
        this.fluids = fluids;
    }
}
