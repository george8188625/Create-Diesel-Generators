package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.CanisterBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.simibubi.create.api.behaviour.BlockSpoutingBehaviour;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class SpoutCanisterFilling extends BlockSpoutingBehaviour {
    @Override
    public int fillBlock(Level level, BlockPos pos, SpoutBlockEntity spout, FluidStack availableFluid, boolean simulate) {
        if(!ConfigRegistry.CANISTER_SPOUT_FILLING.get())
            return 0;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CanisterBlockEntity be){
            IFluidHandler handler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, Direction.UP).orElse(null);
            if(handler.getFluidInTank(0).isFluidEqual(availableFluid) || handler.getFluidInTank(0).isEmpty())
                return handler.fill(availableFluid, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        }
        return 0;
    }
}
