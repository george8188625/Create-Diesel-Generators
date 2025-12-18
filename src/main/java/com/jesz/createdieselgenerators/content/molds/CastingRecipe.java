package com.jesz.createdieselgenerators.content.molds;

import com.google.gson.JsonObject;
import com.jesz.createdieselgenerators.CDGRecipes;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;

public class CastingRecipe extends ProcessingRecipe<Container> {
    public MoldType moldType;
    public CastingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(CDGRecipes.CASTING, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 0;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxFluidOutputCount() {
        return 0;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }
    public boolean matches(BasinBlockEntity basin, FluidStack fluidStack) {
        if (moldType == null)
            return false;
        if (getFluidIngredients().size() != 1)
            return false;

        IItemHandler availableItems = basin.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElse(null);

        if (availableItems == null)
            return false;

        MoldType moldInBasin = null;
        for (int i = 0; i < availableItems.getSlots(); i++) {
            ItemStack stack = availableItems.getStackInSlot(i);
            if (stack == null)
                continue;

            if (stack.getItem() instanceof MoldItem && MoldItem.getMold(stack) == moldType)
                moldInBasin = MoldItem.getMold(stack);
        }

        if (moldInBasin == null)
            return false;


        if (getFluidIngredients().get(0).test(fluidStack))
            return BasinRecipe.match(basin, this);

        return false;
    }
    @Override
    public void readAdditional(JsonObject json) {
        super.readAdditional(json);
        moldType = MoldType.findById(new ResourceLocation(json.get("mold").getAsString()));
    }

    @Override
    public void readAdditional(FriendlyByteBuf buffer) {
        super.readAdditional(buffer);
        moldType = MoldType.findById(new ResourceLocation(buffer.readUtf()));
    }

    @Override
    public void writeAdditional(JsonObject json) {
        super.writeAdditional(json);
        json.addProperty("mold", moldType.getId().toString());
    }

    @Override
    public void writeAdditional(FriendlyByteBuf buffer) {
        super.writeAdditional(buffer);
        buffer.writeUtf(moldType.getId().toString());
    }

    public int execute(BasinBlockEntity basin, boolean simulate) {
        IItemHandler availableItems = basin.getCapability(ForgeCapabilities.ITEM_HANDLER)
                .orElse(null);

        if (availableItems == null)
            return 0;

        MoldType moldInBasin = null;
        for (int i = 0; i < availableItems.getSlots(); i++) {
            ItemStack stack = availableItems.getStackInSlot(i);

            if (stack.getItem() instanceof MoldItem && MoldItem.getMold(stack) == moldType)
                moldInBasin = MoldItem.getMold(stack);
        }

        if (moldInBasin == null)
            return 0;

        List<ItemStack> recipeOutputItems = new ArrayList<>();

        if (!simulate)
            recipeOutputItems.addAll(rollResults());

        if (!basin.acceptOutputs(recipeOutputItems, List.of(), false))
            return 0;

        return getFluidIngredients().get(0).getRequiredAmount();
    }

}
