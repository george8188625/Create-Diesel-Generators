package com.jesz.createdieselgenerators.blocks.entity;

import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;

import static com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock.FACING;

public class HugeDieselEngineValueBox extends ValueBoxTransform.Sided {

    @Override
    protected boolean isSideActive(BlockState state, Direction side) {
        return !state.getValue(BooleanProperty.create(side.toString())) && state.getValue(FACING).getAxis() != side.getAxis();
    }

    @Override
    public Vec3 getLocalOffset(BlockState state) {
        Vec3 location = new Vec3(0.5, 0.5, 0.9485);
        location = VecHelper.rotateCentered(location, AngleHelper.horizontalAngle(getSide()), Direction.Axis.Y);
        location = VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
        return location;
    }

    @Override
    protected Vec3 getSouthLocation() {
        return Vec3.ZERO;
    }
}
