package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGTags;
import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.concrete.ConcreteEncasedFluidPipeBlock;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.fluids.FluidFX;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

import static com.simibubi.create.AllTags.optionalTag;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class PumpjackHoleBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {
    BlockState state;

    SmartFluidTankBehaviour tank;
    public int headPos = 0;
    public int bearingPos = 0;
    public boolean started = false;
    public PumpjackHoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = state;
    }

    @OnlyIn(Dist.CLIENT)
    protected AbstractTickableSoundInstance soundInstance;

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("OilAmount", oilAmount);
        tag.putBoolean("Started", started);
    }

    public int oilAmount = 0;

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (valid)
            return false;

        Lang.builder(CreateDieselGenerators.ID).translate("hint.pumpjack_hole_no_pipe.title").style(ChatFormatting.GOLD).forGoggles(tooltip);
        Component hint = Lang.builder(CreateDieselGenerators.ID).translate("hint.pumpjack_hole_no_pipe").component();
        List<Component> cutComponent = TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
        for (Component component : cutComponent)
            CreateLang.builder().add(component).forGoggles(tooltip);
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (!valid || !started)
            return false;
        if (oilAmount == Integer.MAX_VALUE) {
            TooltipHelper.addHint(tooltip, "hint.hose_pulley");
            return true;
        }

        CreateLang.builder().add(Component.translatable("createdieselgenerators.goggle.oil_amount")).style(ChatFormatting.GRAY).forGoggles(tooltip);
        CreateLang.text(String.format("%,d", oilAmount)).add(CreateLang.translate("generic.unit.millibuckets")).style(ChatFormatting.GOLD).forGoggles(tooltip);

        return true;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        oilAmount = tag.getInt("OilAmount");
        started = tag.getBoolean("Started");
    }

    byte tick = 0;
    public int pipeLength = 0;
    boolean valid = false;
    @Override
    public void tick() {
        super.tick();
        tick++;
        if (tick >= 20) {
            int pipeLength = 0;
            tick = 0;
            boolean valid = false;
            for (int i = 0; i < getBlockPos().getY() - level.getMinBuildHeight(); i++) {
                pipeLength++;
                BlockState bs = level.getBlockState(getBlockPos().below(i + 1));
                if (bs.getBlock() instanceof PipeBlock || bs.getBlock() instanceof EncasedPipeBlock || bs.getBlock() instanceof ConcreteEncasedFluidPipeBlock) {
                    if (!(bs.getValue(BlockStateProperties.UP) && bs.getValue(BlockStateProperties.DOWN)))
                        break;
                } else if(bs.getBlock() instanceof GlassFluidPipeBlock) {
                    if (!(bs.getValue(AXIS) == Direction.Axis.Y))
                        break;
                } else if (bs.is(CDGTags.PUMPJACK_PIPE)){
                    continue;
                } else if (bs.is(CDGTags.OIL_DEPOSIT)) {
                    valid = true;
                    break;
                } else
                    break;
            }
            if (valid)
                this.pipeLength = pipeLength;
            else
                this.pipeLength = 0;
            this.valid = valid;
        }
        if (level.isClientSide)
            CatnipServices.PLATFORM.executeOnClientOnly(() -> this::tickClient);
    }

    @OnlyIn(Dist.CLIENT)
    public static class PumpjackSoundInstance extends AbstractTickableSoundInstance {
        public PumpjackSoundInstance(SoundEvent event, float volume, float pitch, Vec3 pos) {
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

    @OnlyIn(Dist.CLIENT)
    protected void tickClient() {
        boolean isActive = started && valid && oilAmount > 0;

        if (isActive) {
            if (soundInstance == null || soundInstance.isStopped()) {
                soundInstance = new PumpjackSoundInstance(
                        SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 0.2f, 0.4f,
                        Vec3.atCenterOf(getBlockPos()));
                Minecraft.getInstance().getSoundManager().play(soundInstance);
            }
        } else {
            if (soundInstance != null) {
                Minecraft.getInstance().getSoundManager().stop(soundInstance);
                soundInstance = null;
            }
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(pipeLength);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 8000);
        behaviours.add(tank);
    }

    public void pumpjackRotation(boolean isCrankLarge) {

        List<Fluid> stackList = new ArrayList<>();
        BuiltInRegistries.FLUID.getTags()
                .filter(p -> p.getFirst().equals(optionalTag(BuiltInRegistries.FLUID, CreateDieselGenerators.rl("pumpjack_output"))))
                .map(Pair::getSecond)
                .forEach(s -> stackList.addAll(s.stream().map(Holder::value).toList()));

        if (stackList.isEmpty())
            return;

        if (!level.isClientSide && valid) {
            ChunkPos chunkPos = new ChunkPos(getBlockPos());
            oilAmount = OilChunksSavedData.getChunkOilAmount((ServerLevel) level, chunkPos);
            started = true;

            int subtractedAmount = Mth.clamp((int) (1000 * Math.abs((float) headPos / (float) bearingPos)) * (isCrankLarge ? 2 : 1), 0, oilAmount);

            FluidStack oilStack = new FluidStack(stackList.get(0), subtractedAmount);

            subtractedAmount = tank.getPrimaryHandler().fill(oilStack, IFluidHandler.FluidAction.EXECUTE);

            if (oilAmount == Integer.MAX_VALUE)
                return;

            oilAmount -= subtractedAmount;
            OilChunksSavedData.setChunkOilAmount((ServerLevel) level, chunkPos, oilAmount);
        }

        if (level.isClientSide && oilAmount > 0)
            FluidFX.spawnPouringLiquid(level, worldPosition, 20, FluidFX.getFluidParticle(new FluidStack(stackList.get(0), 1000)), 0.3f, new Vec3(0.1, 1, 0.1), true);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                CDGBlockEntityTypes.PUMPJACK_HOLE.get(),
                (be, side) -> {
                    if (side == null || (side.getAxis().isHorizontal() && be.getBlockState().getValue(
                            side == Direction.NORTH ? NORTH :
                            side == Direction.EAST ? EAST :
                            side == Direction.WEST ? WEST : SOUTH
                    )))
                        return be.tank.getCapability();
                    return null;
                }
        );
    }
}
