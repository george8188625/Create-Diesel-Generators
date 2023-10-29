package com.jesz.createdieselgenerators.blocks.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LargeDieselGeneratorValueBox extends ValueBoxTransform.Sided {

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        return side == Direction.UP;
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Vec3 local = VecHelper.voxelSpace(8, 16, 8);

        return local;
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
