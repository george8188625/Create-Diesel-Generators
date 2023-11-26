package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.jesz.createdieselgenerators.sounds.SoundRegistry;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.SILENCED;
import static com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock.*;

public class LargeDieselGeneratorBlockEntity extends GeneratingKineticBlockEntity {
    BlockState state;
    public boolean validFuel;
    public int stacked;
    public boolean powered = false;
    boolean end = true;
    public WeakReference<LargeDieselGeneratorBlockEntity> forw;
    public WeakReference<LargeDieselGeneratorBlockEntity> back;

    public LargeDieselGeneratorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        forw = new WeakReference<>(null);
        back = new WeakReference<>(null);

        this.state = state;
    }

    public SmartFluidTankBehaviour tank;

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (computerBehaviour.isPeripheralCap(cap))
            return computerBehaviour.getPeripheralCapability();
        if (state.getValue(PIPE)) {
            if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side == Direction.UP)
                if(FrontEngine != null)
                    return FrontEngine.tank.getCapability().cast();
                else
                    return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap) {
        if(cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return tank.getCapability().cast();
        return super.getCapability(cap);
    }
    int partialSecond;
    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("PartialSecond", partialSecond);
        tank.write(compound, false);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        partialSecond = compound.getInt("PartialSecond");
        tank.read(compound, false);
    }
    public ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    public AbstractComputerBehaviour computerBehaviour;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));

        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                Lang.translateDirect("contraptions.windmill.rotation_direction"), this, new LargeDieselGeneratorValueBox());
        movementDirection.withCallback($ -> onDirectionChanged(true));

        behaviours.add(movementDirection);
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }
    public void onDirectionChanged(boolean first) {
        if(first && getEngineFor() != null){
            FrontEngine.movementDirection.setValue(movementDirection.getValue());
            FrontEngine.onDirectionChanged(false);
            return;
        }
        movementDirection.setValue(FrontEngine.movementDirection.getValue());
        if(getEngineBack() != null)
            getEngineBack().onDirectionChanged(false);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }

    @Override
    public float calculateAddedStressCapacity() {
        if (getGeneratedSpeed() == 0 || !end)
            return 0;

        return FuelTypeManager.getGeneratedStress(this, tank.getPrimaryHandler().getFluid().getFluid()) / Math.abs(getGeneratedSpeed()) * stacked;
    }

    @Override
    public float getGeneratedSpeed() {
        if(!end)
            return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1)* FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()), getBlockState().getValue(LargeDieselGeneratorBlock.FACING));
    }
    public LargeDieselGeneratorBlockEntity FrontEngine;
    public void UpdateStacked(){
        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();

        if(engineBack == null) {
            totalSize = 1;
            stacked = 1;

        }else{
            stacked = engineBack.stacked + 1;
        }
        if(engineForward == null) {
            totalSize = stacked;
            SetEveryEnginesFront();
        } else
            engineForward.UpdateStacked();
    }
    public void SetEveryEnginesFront(){
        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();

        if(engineForward == null){
            FrontEngine = this;
        }else{
            FrontEngine = engineForward.FrontEngine;
            totalSize = engineForward.totalSize;
        }
        if(engineBack != null)
            engineBack.SetEveryEnginesFront();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!StressImpact.isEnabled() || FrontEngine == null)
            return added;
        float stressBase = FrontEngine.calculateAddedStressCapacity();
        if (Mth.equal(stressBase, 0))
            return added;
        if(FrontEngine != this){
            Lang.translate("gui.goggles.generator_stats")
                    .forGoggles(tooltip);
            Lang.translate("tooltip.capacityProvided")
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            float stressTotal = Math.abs(FrontEngine.getGeneratedSpeed()* stressBase);

            Lang.number(stressTotal)
                    .translate("generic.unit.stress")
                    .style(ChatFormatting.AQUA)
                    .space()
                    .add(Lang.translate("gui.goggles.at_current_speed")
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

        }
        if(FrontEngine != null)
            return containedFluidTooltip(tooltip, isPlayerSneaking, FrontEngine.tank.getCapability().cast());
        else
            return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
    }
    int t = 0;
    int totalSize = 0;
    @Override
    public void tick() {
        super.tick();


        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();
        state = getBlockState();

        end = engineForward == null;


        updateGeneratedRotation();

        if (reActivateSource) {
            reActivateSource = false;
        }

        // stacked
        if(engineBack == null)
            UpdateStacked();

        if(!tank.isEmpty() && engineForward != null && FrontEngine != null){
            FrontEngine.tank.getPrimaryHandler().fill(tank.getPrimaryHandler().getFluid(), IFluidHandler.FluidAction.EXECUTE);
            tank.getPrimaryHandler().drain(tank.getPrimaryHandler().getFluid(), IFluidHandler.FluidAction.EXECUTE);
        }
        validFuel = FuelTypeManager.getGeneratedSpeed(this, tank.getPrimaryHandler().getFluid().getFluid()) != 0;





        if(FrontEngine != null && t > FuelTypeManager.getSoundSpeed(FrontEngine.tank.getPrimaryHandler().getFluid().getFluid()) && FrontEngine.validFuel && !state.getValue(SILENCED) && (((stacked % 6) == 0) || end)){
            level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundRegistry.DIESEL_ENGINE_SOUND.get(), SoundSource.BLOCKS, 3f,1.08f, false);

            t = 0;
        }else
            t++;

        partialSecond++;
        if(partialSecond >= 20){
            partialSecond = 0;
            if(validFuel)
                if(tank.getPrimaryHandler().getFluid().getAmount() >= FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid()) * stacked)
                    tank.getPrimaryHandler().setFluid(FluidHelper.copyStackWithAmount(tank.getPrimaryHandler().getFluid(),
                            tank.getPrimaryHandler().getFluid().getAmount() - FuelTypeManager.getBurnRate(this, tank.getPrimaryHandler().getFluid().getFluid()) * stacked));
                else
                    tank.getPrimaryHandler().setFluid(FluidStack.EMPTY);
        }
    }

    private void changeBlockState(BlockState state){
        KineticBlockEntity.switchToBlockState(getLevel(), getBlockPos(), state);
    }

    private LargeDieselGeneratorBlockEntity getEngineFor() {
        LargeDieselGeneratorBlockEntity engine = forw.get();
        if (engine == null || engine.isRemoved() || engine.state.getValue(FACING) == state.getValue(FACING)) {
            if (engine != null)
                forw = new WeakReference<>(null);
            Direction facing = this.state.getValue(FACING);
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getAxis() == Direction.Axis.Z ? Direction.SOUTH : Direction.EAST));
            if (be instanceof LargeDieselGeneratorBlockEntity engineBE)
                forw = new WeakReference<>(engine = engineBE);
        }
        if(engine != null){
            if (engine.state.getValue(FACING).getAxis() != state.getValue(FACING).getAxis()) {
                forw = new WeakReference<>(null);
                return null;
            }
        }
        return engine;
    }

    private LargeDieselGeneratorBlockEntity getEngineBack() {
        LargeDieselGeneratorBlockEntity engine = back.get();
        if (engine == null || engine.isRemoved()) {
            if (engine != null)
                back = new WeakReference<>(null);
            Direction facing = this.state.getValue(FACING);
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getAxis() == Direction.Axis.Z ? Direction.NORTH : Direction.WEST));
            if (be instanceof LargeDieselGeneratorBlockEntity engineBE)
                back = new WeakReference<>(engine = engineBE);

        }
        if(engine != null){
            if (engine.state.getValue(FACING).getAxis() != state.getValue(FACING).getAxis()) {
                back = new WeakReference<>(null);
                return null;
            }
        }
        return engine;
    }

}
