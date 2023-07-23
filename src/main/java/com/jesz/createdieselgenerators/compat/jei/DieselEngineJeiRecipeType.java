package com.jesz.createdieselgenerators.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class DieselEngineJeiRecipeType {
    public static final RecipeType<DieselEngineJeiRecipeType> DIESEL_BURNING =
            RecipeType.create("createdieselgenerators", "diesel_burning", DieselEngineJeiRecipeType.class);

    int type;
    float speed;
    List<FluidStack> fluids;
    float stress;
    public DieselEngineJeiRecipeType(int type, float speed, float stress, List<FluidStack> fluids) {
        this.stress = stress;
        this.speed = speed;
        this.type = type;
        this.fluids = fluids;
    }
}
