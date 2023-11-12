package com.jesz.createdieselgenerators.contraption;

import com.jesz.createdieselgenerators.blocks.PumpjackBearingBBlock;
import com.jesz.createdieselgenerators.blocks.PumpjackBearingBlock;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackCrankBlockEntity;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PumpjackBearingBMovementBehaviour implements MovementBehaviour {
    @Nullable
    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public boolean isActive(MovementContext context) {
        if(!(context.contraption instanceof BearingContraption))
            return false;
        if(((BearingContraption) context.contraption).getFacing().getAxis() == Direction.Axis.Y || context.state.getValue(PumpjackBearingBBlock.FACING).getAxis() != ((BearingContraption) context.contraption).getFacing().getClockWise().getAxis())
            return false;
        return context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity;
    }
    @Override
    public void tick(MovementContext context) {
        MovementBehaviour.super.tick(context);
        PumpjackBearingBlockEntity bearing = null;
        if (context.world.getBlockEntity(context.contraption.anchor.relative(((BearingContraption) context.contraption).getFacing().getOpposite())) instanceof PumpjackBearingBlockEntity be)
            bearing = be;
        if(bearing == null)
            return;
        BlockPos crankPos = new BlockPos(
                context.contraption.anchor.getX() + context.localPos.getX(),
                context.contraption.anchor.getY() + context.localPos.getY()-3,
                context.contraption.anchor.getZ() + context.localPos.getZ());
        if(context.world.getBlockEntity(crankPos) instanceof PumpjackCrankBlockEntity crankBE && crankBE.crankSize.getValue() == 0 && (crankBE.getBearing() == bearing || crankBE.getBearing() == null) && crankBE.getBlockState().getValue(FACING).getClockWise().getAxis() == bearing.getBlockState().getValue(BearingBlock.FACING).getAxis()) {
            bearing.bearingBPos = context.localPos;
            bearing.crankPos = crankPos;
            bearing.crankAngle = crankBE.angle;
            bearing.isLarge = false;
        }else{
            crankPos = new BlockPos(
                    context.contraption.anchor.getX() + context.localPos.getX(),
                    context.contraption.anchor.getY() + context.localPos.getY()-4,
                    context.contraption.anchor.getZ() + context.localPos.getZ());
            if(context.world.getBlockEntity(crankPos) instanceof PumpjackCrankBlockEntity crankBE && crankBE.crankSize.getValue() == 1 && (crankBE.getBearing() == bearing || crankBE.getBearing() == null) && crankBE.getBlockState().getValue(FACING).getClockWise().getAxis() == bearing.getBlockState().getValue(BearingBlock.FACING).getAxis()) {
                bearing.bearingBPos = context.localPos;
                bearing.crankPos = crankPos;
                bearing.crankAngle = crankBE.angle;
                bearing.isLarge = true;
            }
        }

        if(context.world.getBlockEntity(crankPos) instanceof PumpjackCrankBlockEntity crankBE) {
            if(bearing.getBlockState().getValue(PumpjackBearingBlock.FACING).getAxis() == Direction.Axis.X)
                crankBE.crankBearingLocation = new Vec3(context.localPos.getZ(), context.localPos.getY(), 0);
            else
                crankBE.crankBearingLocation = new Vec3(context.localPos.getX(), context.localPos.getY(), 0);
            crankBE.bearingAngle = bearing.getInterpolatedAngle(1);
            crankBE.prevBearingAngle = bearing.getInterpolatedAngle(0);
            crankBE.bearingPos = bearing.getBlockPos();
            bearing.crankSpeed = Math.abs(crankBE.getSpeed());
            crankBE.bearing = new WeakReference<>(bearing);


        }
    }
}
