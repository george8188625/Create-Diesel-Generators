package com.jesz.createdieselgenerators.content.pumpjack;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.content.concrete.ConcreteEncasedFluidPipeBlock;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.simibubi.create.AllSoundEvents;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

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
    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.putInt("OilAmount", oilAmount);
        compound.putBoolean("Started", started);
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
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        oilAmount = compound.getInt("OilAmount");
        started = compound.getBoolean("Started");
    }

    @Override
    public void handleUpdateTag(CompoundTag compound) {
        super.handleUpdateTag(compound);
        oilAmount = compound.getInt("OilAmount");
        started = compound.getBoolean("Started");
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
                } else if(bs.is(optionalTag(ForgeRegistries.BLOCKS, new ResourceLocation("createdieselgenerators:pumpjack_pipe")))){
                    continue;
                } else if (bs.is(optionalTag(ForgeRegistries.BLOCKS, new ResourceLocation("createdieselgenerators:oil_deposit")))) {
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

    }

    @Override
    public AABB getRenderBoundingBox() {
        return super.getRenderBoundingBox().inflate(pipeLength);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = super.getUpdateTag();
        compound.putInt("OilAmount", oilAmount);
        compound.putBoolean("Started", started);
        return compound;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 8000);
        behaviours.add(tank);
    }

    public void pumpjackRotation(boolean isCrankLarge) {
        List<Fluid> stackList = ForgeRegistries.FLUIDS.tags()
                .getTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:pumpjack_output")))
                .stream()
                .distinct()
                .toList();
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

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if (!(cap == ForgeCapabilities.FLUID_HANDLER))
            return super.getCapability(cap, side);
        if(side == Direction.NORTH && getBlockState().getValue(NORTH))
            return tank.getCapability().cast();
        if(side == Direction.EAST && getBlockState().getValue(EAST))
            return tank.getCapability().cast();
        if(side == Direction.SOUTH && getBlockState().getValue(SOUTH))
            return tank.getCapability().cast();
        if(side == Direction.WEST && getBlockState().getValue(WEST))
            return tank.getCapability().cast();

        return super.getCapability(cap, side);
    }
}
