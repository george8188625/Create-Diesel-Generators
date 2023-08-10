package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.SILENCED;
import static com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock.*;
import static com.simibubi.create.AllTags.optionalTag;

public class LargeDieselGeneratorBlockEntity extends GeneratingKineticBlockEntity {
    BlockState state;
    boolean weak;
    boolean slow;
    public boolean validFuel;
    public int stacked;
    public boolean powered = false;
    boolean end = true;
    private final TagKey<Fluid> tagSS;
    private final TagKey<Fluid> tagFS;
    private final TagKey<Fluid> tagFW;
    private final TagKey<Fluid> tagSW;
    private final TagKey<Fluid> tagPlantOil;
    private final TagKey<Fluid> tagFuel;
    private final TagKey<Fluid> tagEthanol;
    private final TagKey<Fluid> tagBiodiesel;
    public WeakReference<LargeDieselGeneratorBlockEntity> forw;
    public WeakReference<LargeDieselGeneratorBlockEntity> back;

    public LargeDieselGeneratorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        forw = new WeakReference<>(null);
        back = new WeakReference<>(null);
        tagSS = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_slow_strong"));
        tagSW = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_slow_weak"));
        tagFS = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_fast_strong"));
        tagFW = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_fast_weak"));
        tagPlantOil = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:plantoil"));
        tagFuel = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:fuel"));
        tagEthanol = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:ethanol"));
        tagBiodiesel = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:biodiesel"));

        this.state = state;
    }

    private SmartFluidTankBehaviour tank;
    public LargeDieselGeneratorBlockEntity mainTankBE;

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (state.getValue(PIPE)) {
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.UP)
                if(FrontEngine != null)
                    return FrontEngine.tank.getCapability().cast();
                else
                    return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        tank.write(compound, false);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tank.read(compound, false);
    }
    protected ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
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
        if (weak)
            return (ConfigRegistry.WEAK_STRESS.get().floatValue() / Math.abs(getGeneratedSpeed())) * stacked;
        else
            return (ConfigRegistry.STRONG_STRESS.get().floatValue() / Math.abs(getGeneratedSpeed())) * stacked;
    }

    @Override
    public float getGeneratedSpeed() {
        if (validFuel) {
            if(slow)
                return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1)*ConfigRegistry.SLOW_SPEED.get().floatValue(), getBlockState().getValue(FACING));
            else
                return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1)*ConfigRegistry.FAST_SPEED.get().floatValue(), getBlockState().getValue(FACING));
        }
        return 0;
    }
    public LargeDieselGeneratorBlockEntity FrontEngine;
    public void UpdateStacked(){
        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();


        if(engineBack == null) {
            stacked = 1;

        }else{
            stacked = engineBack.stacked + 1;
        }
        if(engineForward == null)
            SetEveryEnginesFront();
        else
            engineForward.UpdateStacked();
    }
    public void SetEveryEnginesFront(){
        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();

        if(engineForward == null){
            FrontEngine = this;
        }else{
            FrontEngine = engineForward.FrontEngine;
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

        Lang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
        Lang.fluidName(FrontEngine.tank.getPrimaryHandler().getFluid()).style(ChatFormatting.GRAY).space().add(Lang.number(FrontEngine.tank.getPrimaryHandler().getFluid().getAmount()).style(ChatFormatting.DARK_GRAY)).add(Lang.translate("generic.unit.millibuckets").style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip);

        return true;
    }
    int t = 0;
    boolean lastp = true;
    @Override
    public void tick() {
        super.tick();


        LargeDieselGeneratorBlockEntity engineForward = getEngineFor();
        LargeDieselGeneratorBlockEntity engineBack = getEngineBack();
        state = getBlockState();

        end = engineForward == null;




        if (reActivateSource) {
            updateGeneratedRotation();
            reActivateSource = false;
        }

        // stacked
        if(engineBack == null)
            UpdateStacked();

        if(!tank.isEmpty() && engineForward != null && FrontEngine != null){
            FrontEngine.tank.getPrimaryHandler().fill(tank.getPrimaryHandler().getFluid(), IFluidHandler.FluidAction.EXECUTE);
            tank.getPrimaryHandler().drain(tank.getPrimaryHandler().getFluid(), IFluidHandler.FluidAction.EXECUTE);
        }

        if(tank.getPrimaryHandler().getFluid().getAmount() >= stacked){
            if (tank.getPrimaryHandler().getFluid().getFluid().is(tagFS) || (ConfigRegistry.FUEL_TAG.get() && tank.getPrimaryHandler().getFluid().getFluid().is(tagFuel)) || (ConfigRegistry.BIODIESEL_TAG.get() && tank.getPrimaryHandler().getFluid().getFluid().is(tagBiodiesel))) {
                validFuel = true;
                slow = false;
                weak = false;
            } else if (tank.getPrimaryHandler().getFluid().getFluid().is(tagFW) || (ConfigRegistry.ETHANOL_TAG.get() && tank.getPrimaryHandler().getFluid().getFluid().is(tagEthanol))) {
                validFuel = true;
                slow = false;
                weak = true;
            } else if (tank.getPrimaryHandler().getFluid().getFluid().is(tagSS) || (ConfigRegistry.PLANTOIL_TAG.get() && tank.getPrimaryHandler().getFluid().getFluid().is(tagPlantOil))) {
                validFuel = true;
                slow = true;
                weak = false;
            } else if (tank.getPrimaryHandler().getFluid().getFluid().is(tagSW)) {
                validFuel = true;
                slow = true;
                weak = true;
            }else{
                validFuel = false;
            }
        }else{
            validFuel = false;
        }
        lastp = getBlockState().getValue(POWERED);

        if (end) {
            if(lastp != validFuel){
                changeBlockState(getBlockState().setValue(POWERED, powered));
            }
            updateGeneratedRotation();
            powered = getGeneratedSpeed() != 0;
        } else{
            powered = engineForward.powered;
            if (lastp != powered) {
                changeBlockState(getBlockState().setValue(POWERED, powered));
            }
        }


        if (t > 2 && end) {
            if (validFuel && tank.getPrimaryHandler().getFluid().getAmount() >= stacked)
                tank.getPrimaryHandler().setFluid(FluidHelper.copyStackWithAmount(tank.getPrimaryHandler().getFluid(),
                        tank.getPrimaryHandler().getFluid().getAmount() - stacked));
            t = 0;
        } else {
            t++;
        }
        if(t > 2 && powered && !state.getValue(SILENCED)){
            level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.18f, false);
            AllSoundEvents.STEAM.playAt(level, worldPosition, 0.03f, .8f, false);
        }


    }

    private void changeBlockState(BlockState state) {
        level.setBlock(getBlockPos(), state, 3);
    }

    private LargeDieselGeneratorBlockEntity getEngineFor() {
        LargeDieselGeneratorBlockEntity engine = forw.get();
        if (engine == null || engine.isRemoved() || engine.state.getValue(FACING) == state.getValue(FACING)) {
            if (engine != null)
                forw = new WeakReference<>(null);
            Direction facing = this.state.getValue(FACING);
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
            if (be instanceof LargeDieselGeneratorBlockEntity engineBE)
                forw = new WeakReference<>(engine = engineBE);
        }
        if(engine != null){
            if (engine.state.getValue(FACING) != state.getValue(FACING)) {
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
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing));
            if (be instanceof LargeDieselGeneratorBlockEntity engineBE)
                back = new WeakReference<>(engine = engineBE);

        }
        if(engine != null){
            if (engine.state.getValue(FACING) != state.getValue(FACING)) {
                back = new WeakReference<>(null);
                return null;
            }
        }
        return engine;
    }
}
