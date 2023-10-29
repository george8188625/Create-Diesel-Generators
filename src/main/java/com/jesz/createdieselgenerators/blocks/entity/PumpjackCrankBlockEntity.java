package com.jesz.createdieselgenerators.blocks.entity;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.ref.WeakReference;
import java.util.List;

public class PumpjackCrankBlockEntity extends KineticBlockEntity {
    public float angle = 0;
    public float prevAngle = 0;
    public float bearingAngle;
    public float prevBearingAngle;
    public BlockPos bearingPos;
    public WeakReference<PumpjackBearingBlockEntity> bearing = new WeakReference<>(null);

    public PumpjackCrankBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        angle = compound.getFloat("Angle");
        super.read(compound, clientPacket);
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        compound.putFloat("Angle", angle);
        super.write(compound, clientPacket);
    }

    @Override
    public float calculateStressApplied() {
        float impact = (float) 16;
        this.lastStressApplied = impact;
        return impact;
    }
    public PumpjackBearingBlockEntity getBearing(){
        if(bearing.get() != null){
            if(bearing.get().isRemoved()) {
                bearing = new WeakReference<>(null);
                return null;
            }
            return bearing.get();
        }
        return null;
    }

    @Override
    public void tick() {
        super.tick();
        PumpjackBearingBlockEntity bearing = getBearing();
        if(bearing != null){
            if(bearing.isStalled())
                return;
        }
        prevAngle = angle;
        if(angle >= 359 || angle <= -359)
            angle = 0;
        if(getSpeed() != 0)
            angle += Mth.clamp(Math.abs(getSpeed()), 0, 64) / 10;
    }
    public Vec3 crankBearingLocation = new Vec3(0, -100, 0);

    public ScrollOptionBehaviour<CrankSize> crankSize;
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        crankSize = new ScrollOptionBehaviour<>(CrankSize.class,
                Components.translatable("createdieselgenerators.pumpjack_crank.crank_size"), this, new PumpjackCrankValueBox());
        crankSize.withCallback($ -> onSizeChanged());
        behaviours.add(crankSize);
        super.addBehaviours(behaviours);
    }

    private void onSizeChanged() {

    }
    public enum CrankSize implements INamedIconOptions {
        NORMAL(AllIcons.I_CLEAR), LARGE(AllIcons.I_PLACE);

        private final AllIcons icon;
        CrankSize(AllIcons icon) {
            this.icon = icon;
        }
        @Override
        public AllIcons getIcon() {
            return icon;
        }

        @Override
        public String getTranslationKey() {
            return "tooltip.capacityProvided."+ (icon == AllIcons.I_CLEAR ? "low" : "high");
        }
    }
}
