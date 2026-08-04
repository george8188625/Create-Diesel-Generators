package com.jesz.createdieselgenerators.content.distillation;

import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.item.SmartInventory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;

public class DistillationRecipe extends StandardProcessingRecipe<RecipeInput> {
    public DistillationRecipe(ProcessingRecipeParams params){
        super(CDGRecipes.DISTILLATION, params);

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
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }
}
