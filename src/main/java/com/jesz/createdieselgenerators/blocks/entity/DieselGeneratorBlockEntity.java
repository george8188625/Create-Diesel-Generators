package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
//import com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock;
import com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.*;
import static com.simibubi.create.AllTags.optionalTag;

public class DieselGeneratorBlockEntity extends GeneratingKineticBlockEntity {
    BlockState state;
    boolean weak;
    boolean slow;
    boolean validFuel;

    private final TagKey<Fluid> tagSS;
    private final TagKey<Fluid> tagFS;
    private final TagKey<Fluid> tagFW;
    private final TagKey<Fluid> tagSW;
    private final TagKey<Fluid> tagPlantOil;
    private final TagKey<Fluid> tagFuel;
    private final TagKey<Fluid> tagEthanol;
    public DieselGeneratorBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        tagSS = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_slow_strong"));
        tagSW = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_slow_weak"));
        tagFS = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_fast_strong"));
        tagFW = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:diesel_engine_fuel_fast_weak"));
        tagPlantOil = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:plantoil"));
        tagFuel = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:fuel"));
        tagEthanol = optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("forge:ethanol"));
        this.state = state;
    }
    private SmartFluidTankBehaviour tank;





    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(state.getValue(FACING) == Direction.DOWN) {
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.WEST)
                return tank.getCapability().cast();
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.EAST)
                return tank.getCapability().cast();
        }else if(state.getValue(FACING) == Direction.UP){
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.NORTH)
                return tank.getCapability().cast();
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.SOUTH)
                return tank.getCapability().cast();
        }else{
            if (cap == ForgeCapabilities.FLUID_HANDLER && side == Direction.DOWN)
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

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!hasSource() || getGeneratedSpeed() > getTheoreticalSpeed())
            updateGeneratedRotation();
    }
    @Override
    public float calculateAddedStressCapacity() {
        if(getGeneratedSpeed() == 0)
            return 0;
        if(weak)
            return 512/Math.abs(getGeneratedSpeed());
        else
            return 1024/Math.abs(getGeneratedSpeed());
    }

    @Override
    public float getGeneratedSpeed() {
        if(validFuel) {
            if(slow)
                return convertToDirection(48, getBlockState().getValue(FACING));
            else
                return convertToDirection(96, getBlockState().getValue(FACING));
        }
        return 0;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (!IRotate.StressImpact.isEnabled())
            return added;

        float stressBase = calculateAddedStressCapacity();
        if (Mth.equal(stressBase, 0))
            return added;
        Lang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
        Lang.fluidName(tank.getPrimaryHandler().getFluid()).style(ChatFormatting.GRAY).space().add(Lang.number(tank.getPrimaryHandler().getFluid().getAmount()).style(ChatFormatting.DARK_GRAY)).add(Lang.translate("generic.unit.millibuckets").style(ChatFormatting.DARK_GRAY)).forGoggles(tooltip);

        return true;
    }
    int t = 0;
    float lastsp = 0;
    @Override
    public void tick() {
        super.tick();
        updateGeneratedRotation();
        if (tank.getPrimaryHandler().getFluid().getFluid().is(tagFS) || tank.getPrimaryHandler().getFluid().getFluid().is(tagFuel)) {
            validFuel = true;
            slow = false;
            weak = false;
        } else if (tank.getPrimaryHandler().getFluid().getFluid().is(tagFW) || tank.getPrimaryHandler().getFluid().getFluid().is(tagEthanol)) {
            validFuel = true;
            slow = false;
            weak = true;
        } else if (tank.getPrimaryHandler().getFluid().getFluid().is(tagSS) || tank.getPrimaryHandler().getFluid().getFluid().is(tagPlantOil)) {
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

        if(lastsp != getGeneratedSpeed()){
            changeBlockstate(state.setValue(DieselGeneratorBlock.POWERED, validFuel));
            lastsp = getGeneratedSpeed();
        }

        if(t > 2){
            if(validFuel){
                tank.getPrimaryHandler().setFluid(FluidHelper.copyStackWithAmount(tank.getPrimaryHandler().getFluid(),
                        tank.getPrimaryHandler().getFluid().getAmount() - 1));
                t = 0;
                level.playLocalSound(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 3f, 1.18f, false);
                AllSoundEvents.STEAM.playAt(level, worldPosition, 0.2f, .8f, false);
            }
        }else {
            t++;
        }


    }
    private void changeBlockstate(BlockState state){
        level.setBlock(getBlockPos(), state, 3);
    }
}
