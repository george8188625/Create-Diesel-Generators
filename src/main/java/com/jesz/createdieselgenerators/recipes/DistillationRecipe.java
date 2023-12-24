package com.jesz.createdieselgenerators.recipes;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.world.level.Level;

public class DistillationRecipe extends ProcessingRecipe<SmartInventory> {
    public DistillationRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params){
        super(RecipeRegistry.DISTILLATION, params);

    }
    @Override
    protected int getMaxInputCount() {
        return 0;
    }

    @Override
    protected int getMaxOutputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 6;
    }

    @Override
    protected boolean canRequireHeat() {
        return true;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    @Override
    public boolean matches(SmartInventory p_44002_, Level p_44003_) {
        return false;
    }
}
