package com.jesz.createdieselgenerators.content.pumpjack;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;
import java.util.List;

public class PumpjackCrankBlockEntity extends KineticBlockEntity {
    public float angle = 0;
    public float prevAngle = 0;
    public float bearingAngle;
    public float prevBearingAngle;
    public BlockPos bearingPos;
    public WeakReference<PumpjackBearingBlockEntity> bearing = new WeakReference<>(null);
    public float inPonderAngle = Integer.MIN_VALUE;
    @OnlyIn(Dist.CLIENT)
    protected AbstractTickableSoundInstance rumbleInstance;
    @OnlyIn(Dist.CLIENT)
    protected AbstractTickableSoundInstance hissInstance;
    private float lastSoundAngle = 0;
    public PumpjackCrankBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        angle = compound.getFloat("Angle");
        crankBearingLocation = new Vec3(
                compound.getDouble("BackPosX"),
                compound.getDouble("BackPosY"),
                compound.getDouble("BackPosZ"));
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putFloat("Angle", angle);
        compound.putDouble("BackPosX", crankBearingLocation.x);
        compound.putDouble("BackPosY", crankBearingLocation.y);
        compound.putDouble("BackPosZ", crankBearingLocation.z);
    }

    @Override
    public float calculateStressApplied() {
        float impact = 16f;
        this.lastStressApplied = impact;
        return impact;
    }

    public PumpjackBearingBlockEntity getBearing(){
        if(bearing.get() != null){
            if(bearing.get().isRemoved() || !bearing.get().isRunning()) {
                bearing = new WeakReference<>(null);
                return null;
            }
            return bearing.get();
        }
        return null;
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(3);
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

        if (level.isClientSide)
            CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickClient);
    }
    public Vec3 crankBearingLocation = new Vec3(0, -100, 0);

    public ScrollOptionBehaviour<CrankSize> crankSize;
    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        crankSize = new ScrollOptionBehaviour<>(CrankSize.class,
                Component.translatable("createdieselgenerators.pumpjack_crank.crank_size"), this, new PumpjackCrankValueBox());
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
            return "createdieselgenerators.tooltip.crank." + Lang.asId(name());
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickClient() {
        boolean isActive = getSpeed() != 0 && getBearing() != null;

        if (isActive) {
            if (rumbleInstance == null || rumbleInstance.isStopped()) {
                rumbleInstance = new CrankSoundInstance(
                        SoundEvents.MINECART_RIDING, 0.3f, 0.4f,
                        Vec3.atCenterOf(getBlockPos()));
                Minecraft.getInstance().getSoundManager().play(rumbleInstance);
            }
            if (hissInstance == null || hissInstance.isStopped()) {
                hissInstance = new CrankSoundInstance(
                        SoundEvents.BLASTFURNACE_FIRE_CRACKLE, 0.1f, 1.2f,
                        Vec3.atCenterOf(getBlockPos()));
                Minecraft.getInstance().getSoundManager().play(hissInstance);
            }

            float prevNorm = prevAngle % 360;
            float currNorm = angle % 360;

            boolean crossedBottom = (prevNorm < 10 && currNorm >= 10) || (prevNorm > 350 && currNorm <= 10);
            boolean crossedTop = (prevNorm < 190 && currNorm >= 190) || (prevNorm > 170 && currNorm <= 170);

            if (crossedBottom) {
                Minecraft.getInstance().level.playLocalSound(getBlockPos(),
                        SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.2f, 0.3f, false);
                Minecraft.getInstance().level.playLocalSound(getBlockPos(),
                        SoundEvents.ANVIL_HIT, SoundSource.BLOCKS, 0.1f, 0.5f, false);
            }

            if (crossedTop) {
                Minecraft.getInstance().level.playLocalSound(getBlockPos(),
                        SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.15f, 0.3f, false);
                Minecraft.getInstance().level.playLocalSound(getBlockPos(),
                        SoundEvents.CHAIN_STEP, SoundSource.BLOCKS, 0.1f, 0.6f, false);
            }

            if (crossedBottom && Math.random() < 0.3)
                Minecraft.getInstance().level.playLocalSound(getBlockPos(),
                        SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 0.15f, 0.5f, false);

        } else {
            if (rumbleInstance != null) {
                Minecraft.getInstance().getSoundManager().stop(rumbleInstance);
                rumbleInstance = null;
            }
            if (hissInstance != null) {
                Minecraft.getInstance().getSoundManager().stop(hissInstance);
                hissInstance = null;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class CrankSoundInstance extends AbstractTickableSoundInstance {
        public CrankSoundInstance(SoundEvent event, float volume, float pitch, Vec3 pos) {
            super(event, SoundSource.BLOCKS, RandomSource.create());
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
            this.volume = volume;
            this.pitch = pitch;
            this.looping = true;
            this.delay = 0;
            this.attenuation = Attenuation.LINEAR;
        }

        @Override
        public void tick() {}
    }
}
