package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.other.FuelTypeManager;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.level.material.Fluid;

public class DieselEngineJeiRecipeType {
    public static final RecipeType<DieselEngineJeiRecipeType> DIESEL_COMBUSTION =
            RecipeType.create("createdieselgenerators", "diesel_combustion", DieselEngineJeiRecipeType.class);

    public int burnRate;
    public float speed;
    public Fluid fluid;
    public float stress;
    public DieselEngineJeiRecipeType(Fluid fluid) {
        this.fluid = fluid;
        speed = FuelTypeManager.getGeneratedSpeed(fluid);
        stress = FuelTypeManager.getGeneratedStress(fluid);
        burnRate = FuelTypeManager.getBurnRate(fluid);
    }
}
