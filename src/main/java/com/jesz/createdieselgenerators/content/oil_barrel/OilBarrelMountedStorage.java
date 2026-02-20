package com.jesz.createdieselgenerators.content.oil_barrel;

import com.jesz.createdieselgenerators.CDGMountedStorageTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class OilBarrelMountedStorage extends WrapperMountedFluidStorage<OilBarrelMountedStorage.Handler> implements SyncedMountedStorage {
    public static final Codec<OilBarrelMountedStorage> CODEC = RecordCodecBuilder.create(i -> i.group(
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(OilBarrelMountedStorage::getCapacity),
            FluidStack.CODEC.fieldOf("fluid").forGetter(OilBarrelMountedStorage::getFluid)
    ).apply(i, OilBarrelMountedStorage::new));

    private boolean dirty;

    protected OilBarrelMountedStorage(MountedFluidStorageType<?> type, int capacity, FluidStack stack) {
        super(type, new OilBarrelMountedStorage.Handler(capacity, stack));
        this.wrapped.onChange = () -> this.dirty = true;
    }

    public OilBarrelMountedStorage(int capacity, FluidStack stack) {
        this(CDGMountedStorageTypes.OIL_BARREL.get(), capacity, stack);
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof OilBarrelBlockEntity tank && tank.isController()) {
            FluidTank inventory = tank.tankInventory;
            if (inventory != null)
                inventory.setFluid(this.wrapped.getFluid());
        }
    }

    public FluidStack getFluid() {
        return this.wrapped.getFluid();
    }

    public int getCapacity() {
        return this.wrapped.getCapacity();
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void markClean() {
        this.dirty = false;
    }

    @Override
    public void afterSync(Contraption contraption, BlockPos localPos) {
        BlockEntity be = contraption.getBlockEntityClientSide(localPos);
        if (!(be instanceof FluidTankBlockEntity tank))
            return;

        FluidTank inv = tank.getTankInventory();
        inv.setFluid(this.getFluid());
        float fillLevel = inv.getFluidAmount() / (float) inv.getCapacity();
        if (tank.getFluidLevel() == null) {
            tank.setFluidLevel(LerpedFloat.linear().startWithValue(fillLevel));
        }
        tank.getFluidLevel().chase(fillLevel, 0.5, LerpedFloat.Chaser.EXP);
    }

    public static OilBarrelMountedStorage fromTank(OilBarrelBlockEntity tank) {
        // tank has update callbacks, make an isolated copy
        FluidTank inventory = tank.tankInventory;
        return new OilBarrelMountedStorage(inventory.getCapacity(), inventory.getFluid().copy());
    }

    public static OilBarrelMountedStorage fromLegacy(CompoundTag nbt) {
        int capacity = nbt.getInt("Capacity");
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
        return new OilBarrelMountedStorage(capacity, fluid);
    }

    public static final class Handler extends FluidTank {
        private Runnable onChange = () -> {};

        public Handler(int capacity, FluidStack stack) {
            super(capacity);
            this.setFluid(stack);
        }

        @Override
        protected void onContentsChanged() {
            this.onChange.run();
        }
    }
}
