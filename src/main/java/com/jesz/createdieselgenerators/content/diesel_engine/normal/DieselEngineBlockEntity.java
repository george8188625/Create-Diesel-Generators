package com.jesz.createdieselgenerators.content.diesel_engine.normal;

import com.jesz.createdieselgenerators.content.diesel_engine.EngineSoundInstance;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.jesz.createdieselgenerators.content.diesel_engine.IEngine;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.DistExecutor;

import java.util.List;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.FACING;

public class DieselEngineBlockEntity extends GeneratingKineticBlockEntity implements IEngine {
    ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;

    float remainingTicks = 0;
    EngineUpgrades upgrade = EngineUpgrades.EMPTY;
    SmartFluidTankBehaviour tank;

    public DieselEngineBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (cap != ForgeCapabilities.FLUID_HANDLER)
            return super.getCapability(cap, side);
        if (getBlockState().getValue(FACING) == Direction.DOWN) {
            if (side.getAxis() == Direction.Axis.X)
                return tank.getCapability().cast();
        } else if(getBlockState().getValue(FACING) == Direction.UP) {
            if (side.getAxis() == Direction.Axis.Z)
                return tank.getCapability().cast();
        } else {
            if (side == Direction.DOWN)
                return tank.getCapability().cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void write(CompoundTag tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putFloat("RemainingTicks", remainingTicks);
        tag.putString("Upgrade", upgrade.getId().toString());
    }

    @Override
    protected void read(CompoundTag tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        remainingTicks = tag.getFloat("RemainingTicks");
        upgrade = EngineUpgrades.get(new ResourceLocation(tag.getString("Upgrade")));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new DieselEngineValueBox());
        movementDirection.withCallback(v -> reActivateSource = true);
        tank = SmartFluidTankBehaviour.single(this, 1000);

        behaviours.add(movementDirection);
        behaviours.add(tank);
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = upgrade.getCapacity(getFuelCapacity() * (1 / upgrade.getSpeed(getFuelSpeed(), this)) * getFuelSpeed(), this);
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!enabled())
            return 0;
        return convertToDirection((movementDirection.getValue() == 1 ? -1 : 1) * upgrade.getSpeed(getFuelSpeed(), this), getBlockState().getValue(FACING));
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (getGeneratedSpeed() == 0)
            return false;
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability().cast());
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        reActivateSource = true;

        if (enabled()) {
            if (remainingTicks < 2) {
                remainingTicks += 1 / getFuelBurnRate();
                tank.getPrimaryHandler().drain(1, IFluidHandler.FluidAction.EXECUTE);
            }

            if (remainingTicks >= 0)
                remainingTicks--;
        }

        if (level.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tickClient);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected EngineSoundInstance soundInstance;
    @OnlyIn(Dist.CLIENT)
    protected void tickClient() {
        if (enabled()) {
            if (soundInstance == null || soundInstance.isStopped()) {
                Minecraft.getInstance()
                        .getSoundManager()
                        .play(soundInstance = upgrade.createSoundInstance(this, Vec3.atCenterOf(getBlockPos())));
            } else if (soundInstance.active()) {
                soundInstance.keepAlive();
                soundInstance.setPitch(upgrade.getPitchMultiplier(this) * getFuelSoundPitch());
                soundInstance.setVolume(upgrade.getVolume(this));
            }
        } else {
            if (soundInstance != null) {
                soundInstance.fadeOut();
                soundInstance = null;
            }
        }
    }

    @Override
    public float getRemainingTicks() {
        return remainingTicks;
    }

    @Override
    public SmartBlockEntity self() {
        return this;
    }

    @Override
    public FluidTank getTank() {
        return tank.getPrimaryHandler();
    }

    @Override
    public EngineUpgrades getUpgrade() {
        return upgrade;
    }

    @Override
    public void setUpgrade(EngineUpgrades upgrade) {
        this.upgrade = upgrade;
    }
}
