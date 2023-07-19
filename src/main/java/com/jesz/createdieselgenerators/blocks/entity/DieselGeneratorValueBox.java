package com.jesz.createdieselgenerators.blocks.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.FACING;

public class DieselGeneratorValueBox extends ValueBoxTransform.Sided {

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        if(state.getValue(FACING) == Direction.UP)
            return side == Direction.WEST;
        if(state.getValue(FACING) == Direction.DOWN)
            return side == Direction.NORTH;
        return side == Direction.UP;
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        if(state.getValue(FACING) == Direction.UP)
            return VecHelper.voxelSpace(3, 8, 8);
        if(state.getValue(FACING) == Direction.DOWN)
            return VecHelper.voxelSpace(8, 8, 3);
        return  VecHelper.voxelSpace(8, 13, 8);
    }

    @Override
    public void rotate(BlockState state, PoseStack ms) {
        super.rotate(state,ms);

    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }
}
