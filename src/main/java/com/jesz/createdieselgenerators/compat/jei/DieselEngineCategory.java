package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.jei.EmptyBackground;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

import static com.simibubi.create.compat.jei.category.CreateRecipeCategory.*;

@ParametersAreNonnullByDefault
public class DieselEngineCategory implements IRecipeCategory<DieselEngineJeiRecipeType> {
    IGuiHelper guiHelper;
    AnimatedDieselEngineElement engine = new AnimatedDieselEngineElement();
    public DieselEngineCategory(IGuiHelper helper) {
        this.guiHelper = helper;

    }

    @Override
    public RecipeType<DieselEngineJeiRecipeType> getRecipeType() {
        return DieselEngineJeiRecipeType.DIESEL_COMBUSTION;
    }

    @Override
    public Component getTitle() {
        return Components.translatable("createdieselgenerators.recipe.diesel_combustion");
    }

    @Override
    public IDrawable getBackground() {
        return new EmptyBackground(177,70);
    }

    @Override
    public IDrawable getIcon() {
        return guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, BlockRegistry.DIESEL_ENGINE.asStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DieselEngineJeiRecipeType recipe, IFocusGroup iFocusGroup) {
        builder
                .addSlot(RecipeIngredientRole.INPUT, 10, 10)
                .setBackground(getRenderedSlot(), -1, -1)
                .addIngredient(ForgeTypes.FLUID_STACK, withImprovedVisibility(new FluidStack(recipe.fluid, 1000)))
                .addTooltipCallback(addFluidTooltip(recipe.burnRate));
    }

    @Override
    public void draw(DieselEngineJeiRecipeType recipe, IRecipeSlotsView iRecipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        AllGuiTextures.JEI_ARROW.render(graphics, 82, 40);
        AllGuiTextures.JEI_SHADOW.render(graphics, 28, 52);
        byte enginesEnabled = (byte) ((DieselGeneratorBlock.EngineTypes.NORMAL.enabled() ? 1 : 0) + (DieselGeneratorBlock.EngineTypes.MODULAR.enabled() ? 1 : 0) + (DieselGeneratorBlock.EngineTypes.HUGE.enabled() ? 1 : 0));
        int currentEngineIndex = (AnimationTickHolder.getTicks() % (120)) / 20;
        List<DieselGeneratorBlock.EngineTypes> enabledEngines = Arrays.stream(DieselGeneratorBlock.EngineTypes.values()).filter(DieselGeneratorBlock.EngineTypes::enabled).toList();
        DieselGeneratorBlock.EngineTypes currentEngine = enabledEngines.get(currentEngineIndex % enginesEnabled);
        float currentSpeed = FuelTypeManager.getGeneratedSpeed(currentEngine, recipe.fluid);
        float currentCapacity = FuelTypeManager.getGeneratedStress(currentEngine, recipe.fluid);
        float currentBurn = FuelTypeManager.getBurnRate(currentEngine, recipe.fluid);

        graphics.drawString(Minecraft.getInstance().font, Lang.number(currentBurn).component().append(Component.translatable("createdieselgenerators.generic.unit.mbps")), 5,
                40, 0x888888, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(currentCapacity/currentSpeed).component().append("x").append(Component.translatable("create.generic.unit.rpm")), 125,
                41, 0x888888, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(currentSpeed).component().append(Component.translatable("create.generic.unit.rpm")), 85,
                33, 0x888888, false);
        graphics.drawString(Minecraft.getInstance().font, Lang.number(currentCapacity).component().append(Component.translatable("create.generic.unit.stress")), 81,
                50, 0x888888, false);
        engine.draw(graphics, 47, 62);
        AllGuiTextures.JEI_DOWN_ARROW.render(graphics, 40, 15);

    }
}