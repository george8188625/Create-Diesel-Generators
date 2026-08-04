package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.content.distillation.DistillationRecipe;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedBlazeBurner;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.CreateLang;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DistillationCategory extends CreateRecipeCategory<DistillationRecipe> {

    private final AnimatedDistillationTower distillationTower = new AnimatedDistillationTower();
    private final AnimatedBlazeBurner heater = new AnimatedBlazeBurner();

    public DistillationCategory(Info<DistillationRecipe> info) {
        super(info);
    }
    private int calculateYOffset(DistillationRecipe recipe) {
        return -23 * (recipe.getMaxFluidOutputCount() - recipe.getFluidResults().size()); //line 49
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DistillationRecipe recipe, IFocusGroup focuses) {

        if(recipe.getFluidIngredients().isEmpty())
            return;
        int yOffset = calculateYOffset(recipe);
        SizedFluidIngredient fluidIngredient = recipe.getFluidIngredients().get(0);
        addFluidSlot(builder, 17, 145 + yOffset, fluidIngredient);


        int i = 1;

        for (FluidStack fluidResult : recipe.getFluidResults()) {
            int yPosition = -23 * i + 150 + yOffset;
            addFluidSlot(builder, 130, yPosition, fluidResult);
            i++;
        }

        HeatCondition requiredHeat = recipe.getRequiredHeat();
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.NONE)) {
            builder
                    .addSlot(RecipeIngredientRole.RENDER_ONLY, 134, 171 + yOffset)
                    .addItemStack(AllBlocks.BLAZE_BURNER.asStack());
        }
        if (!requiredHeat.testBlazeBurner(BlazeBurnerBlock.HeatLevel.KINDLED)) {
            builder
                    .addSlot(RecipeIngredientRole.CATALYST, 153, 171 + yOffset)
                    .addItemStack(AllItems.BLAZE_CAKE.asStack());
        }
    }

    @Override
    public void draw(DistillationRecipe recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        HeatCondition requiredHeat = recipe.getRequiredHeat();
        boolean noHeat = requiredHeat == HeatCondition.NONE;
        int yOffset = calculateYOffset(recipe);
        AllGuiTextures.JEI_ARROW.render(graphics, 40, 150 + yOffset);
        AllGuiTextures shadow = noHeat ? AllGuiTextures.JEI_SHADOW : AllGuiTextures.JEI_LIGHT;
        shadow.render(graphics, 81, 153 + (noHeat ? 10 : 30) + yOffset);
        distillationTower.draw(graphics, 91, 142 + yOffset, recipe.getFluidResults().size());

        if(!noHeat)
            heater.withHeat(requiredHeat.visualizeAsBlazeBurner()).draw(graphics, 91, 150 + yOffset);

        AllGuiTextures heatBar = noHeat ? AllGuiTextures.JEI_NO_HEAT_BAR : AllGuiTextures.JEI_HEAT_BAR;

        heatBar.render(graphics, 4, 170 + yOffset);
        graphics.drawString(Minecraft.getInstance().font, CreateLang.translateDirect(requiredHeat.getTranslationKey()), 9,
                176 + yOffset, requiredHeat.getColor(), false);
    }
}