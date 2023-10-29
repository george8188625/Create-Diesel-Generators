package com.jesz.createdieselgenerators.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class FurnaceBurnItem extends Item {
    int burnTime;
    public FurnaceBurnItem(Properties properties, int burnTime) {
        super(properties);
        this.burnTime = burnTime;
    }
    @Override
    public int getBurnTime(ItemStack stack, RecipeType<?> recipeType) {
        return burnTime;
    }
}
