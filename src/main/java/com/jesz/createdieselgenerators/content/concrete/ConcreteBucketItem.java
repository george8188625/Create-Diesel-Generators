package com.jesz.createdieselgenerators.content.concrete;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.simibubi.create.AllBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class ConcreteBucketItem extends BucketItem {
    DyeColor color;
    public ConcreteBucketItem(DyeColor color, java.util.function.Supplier<? extends Fluid> fluid, Properties properties) {
        super(fluid, properties);
        this.color = color;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState pipeState = context.getLevel().getBlockState(pos);
        if (!AllBlocks.FLUID_PIPE.has(pipeState)) {
            pos = pos.relative(context.getClickedFace());
            pipeState = context.getLevel().getBlockState(pos);
            if (!AllBlocks.FLUID_PIPE.has(pipeState))
                return super.useOn(context);
        }
        context.getLevel().setBlock(pos, ForgeRegistries.BLOCKS.getValue(CreateDieselGenerators.rl(color.getName() + "_concrete_encased_fluid_pipe")).defaultBlockState()
                .setValue(PipeBlock.NORTH, pipeState.getValue(PipeBlock.NORTH))
                .setValue(PipeBlock.EAST, pipeState.getValue(PipeBlock.EAST))
                .setValue(PipeBlock.SOUTH, pipeState.getValue(PipeBlock.SOUTH))
                .setValue(PipeBlock.WEST, pipeState.getValue(PipeBlock.WEST))
                .setValue(PipeBlock.UP, pipeState.getValue(PipeBlock.UP))
                .setValue(PipeBlock.DOWN, pipeState.getValue(PipeBlock.DOWN)), 3);
        this.playEmptySound(context.getPlayer(), context.getLevel(), pos);
        context.getPlayer().setItemInHand(context.getHand(), getEmptySuccessItem(context.getItemInHand(), context.getPlayer()));
        return InteractionResult.SUCCESS;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag tag) {
        return new FluidBucketWrapper(stack);
    }
}
