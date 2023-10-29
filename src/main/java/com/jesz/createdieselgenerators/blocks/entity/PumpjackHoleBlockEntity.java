package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.stream.Collectors;

import static com.simibubi.create.AllTags.optionalTag;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class PumpjackHoleBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    BlockState state;

    SmartFluidTankBehaviour tank;
    public int headPos = 0;
    public int bearingPos = 0;

    public PumpjackHoleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.state = state;
    }
    int storedOil;
    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        tank.write(compound, false);
        compound.putInt("StoredOilAmount", storedOilAmount);
        compound.putInt("OilAmount", oilAmount);

    }
    public int oilAmount = 0;
    public int storedOilAmount = 0;

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        IHaveGoggleInformation.super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if(!valid){
            Lang.builder().add(CreateDieselGenerators.translate("createdieselgenerators.goggle.problem_encountered")).style(ChatFormatting.RED).forGoggles(tooltip);

            Lang.builder().add(CreateDieselGenerators.translate("createdieselgenerators.goggle.pumpjack_invalid_pipes")).style(ChatFormatting.GRAY).forGoggles(tooltip);
            return false;
        }
        Lang.builder().add(CreateDieselGenerators.translate("createdieselgenerators.goggle.oil_amount")).style(ChatFormatting.GRAY).forGoggles(tooltip);
                Lang.number(oilAmount).add(Lang.translate("generic.unit.buckets")).style(ChatFormatting.GOLD).forGoggles(tooltip);

        return true;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tank.read(compound, false);
        storedOilAmount = compound.getInt("StoredOilAmount");
        oilAmount = compound.getInt("OilAmount");


    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        oilAmount = tag.getInt("OilAmount");
    }

    public byte timeOutTime = 0;
    byte tt = 0;
    boolean valid = false;
    @Override
    public void tick() {
        super.tick();
        timeOutTime++;
        if (timeOutTime >= 15) {
            level.setBlock(getBlockPos(), AllBlocks.ENCASED_FLUID_PIPE.getDefaultState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(WEST, state.getValue(WEST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(UP, true).setValue(DOWN, true), 3);
        }
        tt++;
        if (tt >= 20) {
            tt = 0;
            boolean v = false;
            for (int i = 0; i < getBlockPos().getY() - level.getMinBuildHeight(); i++) {
                BlockState bs = level.getBlockState(getBlockPos().below(i + 1));
                if (bs.getBlock() instanceof PipeBlock || bs.getBlock() instanceof EncasedPipeBlock) {
                    if (!(bs.getValue(BlockStateProperties.UP) && bs.getValue(BlockStateProperties.DOWN)))
                        break;
                }else if(bs.getBlock() instanceof GlassFluidPipeBlock){
                    if (!(bs.getValue(AXIS) == Direction.Axis.Y))
                        break;
                } else if (bs.is(Blocks.BEDROCK)) {
                    v = true;
                    break;
                } else
                    break;
            }
            valid = v;
        }

    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = new CompoundTag();
        compound.putInt("OilAmount", oilAmount);
        return compound;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = SmartFluidTankBehaviour.single(this, 1000);
        behaviours.add(tank);
    }
    public void tickFluid(boolean isCrankLarge) {
        if(!level.isClientSide) {
            if(storedOilAmount == 0){
                ChunkPos chunkPos = new ChunkPos(getBlockPos().getX()/16, getBlockPos().getZ()/16);
                int amount = CreateDieselGenerators.getOilAmount(level.getBiome(new BlockPos(chunkPos.x * 16, 64,  chunkPos.z * 16)), chunkPos.x, chunkPos.z, ((ServerLevel)level).getSeed());
                OilChunksSavedData sd = OilChunksSavedData.load((ServerLevel)level);
                if (sd.getChunkOilAmount(chunkPos) >= 0)
                    amount = sd.getChunkOilAmount(chunkPos);
                sd.setChunkAmount(chunkPos, amount-1);
                oilAmount = amount -1;
                storedOilAmount = 1000;
            }
            int subtractedAmount = Mth.clamp((int) (100 * Math.abs((float) headPos / (float) bearingPos)) * (isCrankLarge ? 2 : 1), 0, 1000);
            storedOilAmount = storedOilAmount < subtractedAmount ? 0 : (int) (storedOilAmount - (100 / Math.abs((float) headPos / (float) bearingPos)));
            List<Fluid> stackList = ForgeRegistries.FLUIDS.tags()
                            .getTag(optionalTag(ForgeRegistries.FLUIDS, new ResourceLocation("createdieselgenerators:pumpjack_output")))
                            .stream()
                            .distinct()
                            .toList();;
            if(stackList.isEmpty())
                return;
            FluidStack oilStack = new FluidStack(stackList.get(0), subtractedAmount);

            tank.getPrimaryHandler().fill(oilStack, IFluidHandler.FluidAction.EXECUTE);
        }
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
