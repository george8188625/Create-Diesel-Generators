package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.PumpjackBearingBBlock;
import com.jesz.createdieselgenerators.blocks.PumpjackHeadBlock;
import com.simibubi.create.content.contraptions.AssemblyException;
import com.simibubi.create.content.contraptions.ControlledContraptionEntity;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.content.contraptions.bearing.BearingContraption;
import com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.simibubi.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class PumpjackBearingBlockEntity extends MechanicalBearingBlockEntity {

    public BlockPos bearingBPos = BlockPos.ZERO;
    public BlockPos crankPos = BlockPos.ZERO;
    public boolean isLarge;

    public float crankSpeed;

    public PumpjackBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void applyRotation() {
        movedContraption.setAngle(angle);
    }
    public float crankAngle;
    private float prevAngle;
    @Override
    public float getInterpolatedAngle(float partialTicks) {
        if (isVirtual())
            return Mth.lerp(partialTicks + .5f, prevAngle, angle);
        if (movedContraption == null || movedContraption.isStalled() || !running)
            partialTicks = 0;
        float angularSpeed = getAngularSpeed();
        if (sequencedAngleLimit >= 0)
            angularSpeed = (float) Mth.clamp(angularSpeed, -sequencedAngleLimit, sequencedAngleLimit);
        return Mth.lerp(partialTicks, angle, angle + angularSpeed);
    }

    @Override
    public void assemble() {
        if (getBlockState().getValue(FACING).getAxis() == Direction.Axis.Y)
            return;
        crankPos = BlockPos.ZERO;
        bearingBPos = BlockPos.ZERO;

        crankSpeed = 0;
        angle = 0;
        if (!(level.getBlockState(worldPosition)
                .getBlock() instanceof BearingBlock))
            return;

        Direction direction = getBlockState().getValue(BearingBlock.FACING);
        BearingContraption contraption = new BearingContraption(false, direction);
        AtomicInteger hinges = new AtomicInteger(0);
        AtomicInteger heads = new AtomicInteger(0);
        AtomicInteger hingeDistance = new AtomicInteger(0);
        AtomicInteger hingeHeight = new AtomicInteger(0);

        try {
            contraption.searchMovedStructure(level, getBlockPos().relative(getBlockState().getValue(FACING)), null);
        } catch (AssemblyException e) {
            return;
        }
        contraption.getBlocks().forEach((pos, info) -> {
            if(info.state().getBlock() instanceof PumpjackHeadBlock)
                heads.set(heads.get()+1);
            else if(info.state().getBlock() instanceof PumpjackBearingBBlock && info.state().getValue(PumpjackBearingBBlock.FACING).getAxis() == getBlockState().getValue(FACING).getClockWise().getAxis()){
                int f = Math.abs(pos.getZ());
                if (getBlockState().getValue(FACING).getAxis() == Direction.Axis.Z)
                    f = Math.abs(pos.getX());
                hingeHeight.set(pos.getY());
                hingeDistance.set(f);
                hinges.set(hinges.get()+1);
            }
        });

        if(hinges.get() == 0)
            lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.bearing_missing"));
        else if(hinges.get() > 1)
            lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.too_many_bearings"));
        else if(heads.get() == 0)
            lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.head_missing"));
        else if(heads.get() > 1)
            lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.too_many_heads"));
        else if(hingeDistance.get() < 4)
            lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.arm_too_short"));
        else if(hingeDistance.get() > 16)
            lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.arm_too_long"));
        else if(hingeHeight.get() != 0)
            lastException = new AssemblyException(Component.translatable("createdieselgenerators.gui.assembly.exception.back_bearing_not_centered"));
        else {
            super.assemble();

            return;
        }
        sendData();
    }

    @Override
    public void tick() {
        prevAngle = angle;
        if (level.isClientSide)
            clientAngleDiff /= 2;

        if (!level.isClientSide && assembleNextTick) {
            assembleNextTick = false;
            if (running) {
                if (movedContraption == null || movedContraption.getContraption().getBlocks().isEmpty()) {
                    if (movedContraption != null)
                        movedContraption.getContraption()
                                .stop(level);
                    disassemble();
                    return;
                }
            } else {
                assemble();
            }
        }

        if (!running)
            return;

        if (!(movedContraption != null && movedContraption.isStalled())) {


            int f = Math.abs(bearingBPos.getZ());
            if (getBlockState().getValue(FACING).getAxis() == Direction.Axis.Z)
                f = Math.abs(bearingBPos.getX());

            float b = 0;
            if(f == 4) b = 13f;
            if(f == 5) b = 10;
            if(f == 6) b = 8.2f;
            if(f == 7) b = 7;
            if(f == 8) b = 6;
            if(f == 9) b = 5.3f;
            if(f == 10) b = 4.9f;
            if(f == 11) b = 4.4f;
            if(f == 12) b = 4;
            if(f == 13) b = 3.7f;
            if(f == 14) b = 3.4f;
            if(f == 15) b = 3.2f;
            if(f == 16) b = 3f;

            float[] angleAnimation = {
                    0, 70, 130, 180, 220, 255, 280, 300, 260, 199, 127, 67};
            if(isLarge)
                angleAnimation = new float[]{
                        0, 27, 60, 90, 120, 145, 166, 189, 205, 220, 240, 260, 280, 305, 330, 310, 290, 245, 200, 163, 127, 93, 60, 30};
//                  0, 15, 30, 45,  60,  75,  90, 105, 120, 135, 150, 165, 180, 195, 210, 225, 240, 255, 270, 285, 300,315,330,345
            int lIndex = (int) Math.abs(Math.floor(crankAngle/((double) 360 /angleAnimation.length)) % angleAnimation.length);

            float partialAngle = (float) Math.abs(Math.abs(crankAngle/((double) 360 /angleAnimation.length)) - lIndex);
            float newCrankAngle;

            if ((getBlockState().getValue(FACING).getAxis() == Direction.Axis.Z && bearingBPos.getX() < 0 ) || (getBlockState().getValue(FACING).getAxis() == Direction.Axis.X && bearingBPos.getZ() > 0 ))
                newCrankAngle = AngleHelper.angleLerp(partialAngle, angleAnimation[(angleAnimation.length - lIndex) % angleAnimation.length], angleAnimation[(angleAnimation.length - (lIndex+1)) % angleAnimation.length]);
            else
                newCrankAngle = AngleHelper.angleLerp(partialAngle, angleAnimation[lIndex], angleAnimation[(lIndex+1) % angleAnimation.length]);


            float a = (float) Math.pow(Math.sin((double) newCrankAngle/((double) 360 /angleAnimation.length) /Math.PI / (isLarge ? 4.4 : 2.2)), 2)*2+1;
            if(Math.abs(newCrankAngle) >= 359.5)
                a = 0.99f;

            angle = (a*b-b) * ((getBlockState().getValue(FACING).getAxis() == Direction.Axis.Z ? -1 : 1) * ((getBlockState().getValue(FACING).getAxis() == Direction.Axis.Z ? bearingBPos.getX() : bearingBPos.getZ()) < 0 ? -1 : 1) * (isLarge ? 1.5f : 1));
        }
        if(movedContraption != null)
            applyRotation();


    }
    public boolean isStalled(){
        if(movedContraption == null)
            return false;
        return movedContraption.isStalled();
    }
    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

    @Override
    public void attach(ControlledContraptionEntity contraption) {
        BlockState blockState = getBlockState();
        if (!(contraption.getContraption() instanceof BearingContraption))
            return;
        if (!blockState.hasProperty(FACING))
            return;

        this.movedContraption = contraption;
        setChanged();
        BlockPos anchor = worldPosition.relative(blockState.getValue(FACING));
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        if (!level.isClientSide) {
            this.running = true;
            sendData();
        }
    }

    public void assembleNextTick() {
        assembleNextTick = true;
    }
}
