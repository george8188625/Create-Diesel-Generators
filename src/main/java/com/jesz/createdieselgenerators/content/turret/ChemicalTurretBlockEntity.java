package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGRegistries;
import com.jesz.createdieselgenerators.compat.computercraft.CCProxy;
import com.jesz.createdieselgenerators.content.tools.ChemicalSprayerProjectileEntity;
import com.jesz.createdieselgenerators.fuel_type.FuelType;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.compat.Mods;
import com.simibubi.create.compat.computercraft.AbstractComputerBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.List;

public class ChemicalTurretBlockEntity extends TurretBlockEntity {

    public boolean lighterUpgrade = false;
    public boolean shootNextTick = false;
    public float cogRotation = 0;
    public float lastCogRotation = 0;
    public boolean wasShootingLastTick = false;

    public ChemicalTurretBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public float calculateStressApplied() {
        return 4;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }

    public SmartFluidTankBehaviour tank;
    public AbstractComputerBehaviour computerBehaviour;

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CDGBlockEntityTypes.CHEMICAL_TURRET.get(),
                (be, side) -> {
                    if (side == null || side == Direction.DOWN) {
                        return be.tank.getCapability();
                    }
                    return null;
                }
        );

        if (Mods.COMPUTERCRAFT.isLoaded()) {
            event.registerBlockEntity(
                    PeripheralCapability.get(),
                    CDGBlockEntityTypes.CHEMICAL_TURRET.get(),
                    (be, context) -> be.computerBehaviour.getPeripheralCapability()
            );
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
    }

    public int redstoneSignal;

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide) {
            lastCogRotation = cogRotation;
            if (wasShootingLastTick)
                cogRotation += getSpeed() * 3 / 10f;
        }

        if (!level.isClientSide) {
            boolean wasShooting = wasShootingLastTick;
            wasShootingLastTick = false;

            if (redstoneSignal != 0 || shootNextTick) {
                shootFluids();
                shootNextTick = false;
            }

            if (wasShooting && !wasShootingLastTick)
                sendData();
        }

        if (targetedEntity == null)
            return;
        if (controllingEntity == null) {
            targetedEntity = null;
            return;
        }
        if (Math.abs(targetedHorizontalRotation - horizontalRotation) % 360 <= 2 || Math.abs(targetedHorizontalRotation - horizontalRotation) % 360 >= 358)
            if (Math.abs(targetedVerticalRotation - verticalRotation) <= 2)
                shootFluids();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        lighterUpgrade = compound.getBoolean("LighterUpgrade");
        redstoneSignal = compound.getInt("RedstoneSignal");

        if (clientPacket)
            wasShootingLastTick = compound.getBoolean("WasShooting");
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean("LighterUpgrade", lighterUpgrade);
        compound.putInt("RedstoneSignal", redstoneSignal);

        if (clientPacket)
            compound.putBoolean("WasShooting", wasShootingLastTick);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(computerBehaviour = CCProxy.behaviour(this));
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
        super.addBehaviours(behaviours);
    }

    public void shootFluids() {
        if (getSpeed() == 0) {
            return;
        }

        float shootingForce = getShootingForce();

        if (!level.isClientSide && !tank.isEmpty()) {
            wasShootingLastTick = true;
            sendData();

            AllSoundEvents.MIXING.playOnServer(level, worldPosition, .75f, 1);
            FluidStack fluidStack = tank.getPrimaryHandler().getFluid().copy();

            boolean flammable = FuelType.getTypeFor(level.registryAccess().lookupOrThrow(CDGRegistries.FUEL_TYPE), fluidStack.getFluid()).normal().speed() != 0;
            ChemicalSprayerProjectileEntity projectile = ChemicalSprayerProjectileEntity.spray(level, fluidStack, (flammable && lighterUpgrade) || fluidStack.getFluid().isSame(Fluids.LAVA), fluidStack.getFluid().isSame(Fluids.WATER));

            Vec3 directionVector = new Vec3(
                    - Math.sin(Math.toRadians(horizontalRotation)) * Math.cos(Math.toRadians(-verticalRotation)),
                    Math.sin(Math.toRadians(-verticalRotation)),
                    - Math.cos(Math.toRadians(horizontalRotation)) * Math.cos(Math.toRadians(-verticalRotation))
            );
            Vec3 barrelTip = Vec3.atCenterOf(worldPosition).add(0, 0.625f, 0).add(directionVector.scale(1.25f));
            projectile.setPos(barrelTip);
            projectile.shoot(directionVector.x, directionVector.y, directionVector.z, shootingForce, 5);

            projectile.setOwner(controllingPlayer != null ? controllingPlayer : controllingEntity);

            level.addFreshEntity(projectile);
            if (t == 1) {
                tank.getPrimaryHandler().drain(3, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    public static class ChemicalTurretValueBox extends ValueBoxTransform.Sided {

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 3, 16.05);
        }
        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
