package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.blocks.OilBarrelBlock;
import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.OilBarrelBlockEntity;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import static com.jesz.createdieselgenerators.blocks.OilBarrelBlock.AXIS;

public class OilBarrelBlockItem extends BlockItem {
    public OilBarrelBlockItem(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }

    @Override
    public InteractionResult place(BlockPlaceContext ctx) {
        InteractionResult initialResult = super.place(ctx);
        if (!initialResult.consumesAction())
            return initialResult;
        tryMultiPlace(ctx);
        return initialResult;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos p_195943_1_, Level p_195943_2_, Player p_195943_3_,
                                                 ItemStack p_195943_4_, BlockState p_195943_5_) {
        MinecraftServer minecraftserver = p_195943_2_.getServer();
        if (minecraftserver == null)
            return false;
        CompoundTag nbt = p_195943_4_.getTagElement("BlockEntityTag");
        if (nbt != null) {
            nbt.remove("Size");
            nbt.remove("Height");
            nbt.remove("Controller");
            nbt.remove("LastKnownPos");
            if (nbt.contains("TankContent")) {
                FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("TankContent"));
                if (!fluid.isEmpty()) {
                    fluid.setAmount(Math.min(OilBarrelBlockEntity.getCapacityMultiplier(), fluid.getAmount()));
                    nbt.put("TankContent", fluid.writeToNBT(new CompoundTag()));
                }
            }
        }
        return super.updateCustomBlockEntityTag(p_195943_1_, p_195943_2_, p_195943_3_, p_195943_4_, p_195943_5_);
    }

    private void tryMultiPlace(BlockPlaceContext ctx) {
        Player player = ctx.getPlayer();
        if (player == null)
            return;
        if (player.isSteppingCarefully())
            return;
        Direction face = ctx.getClickedFace();
        ItemStack stack = ctx.getItemInHand();
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockPos placedOnPos = pos.relative(face.getOpposite());
        BlockState placedOnState = world.getBlockState(placedOnPos);

        if (!(placedOnState.getBlock() instanceof OilBarrelBlock))
            return;
        OilBarrelBlockEntity tankAt = ConnectivityHandler.partAt(BlockEntityRegistry.OIL_BARREL.get(), world, placedOnPos);
        if (tankAt == null)
            return;
        OilBarrelBlockEntity controllerBE = tankAt.getControllerBE();
        if (controllerBE == null)
            return;

        int width = controllerBE.getWidth();
        if (width == 1)
            return;

        int tanksToPlace = 0;
        Direction.Axis barrelBlockAxis = placedOnState.getValue(AXIS);
        if (face.getAxis() != barrelBlockAxis)
            return;

        Direction barrelFacing = Direction.fromAxisAndDirection(barrelBlockAxis, Direction.AxisDirection.POSITIVE);
        BlockPos startPos = face == barrelFacing.getOpposite() ? controllerBE.getBlockPos()
                .relative(barrelFacing.getOpposite())
                : controllerBE.getBlockPos()
                .relative(barrelFacing, controllerBE.getHeight());

        if (VecHelper.getCoordinate(startPos, barrelBlockAxis) != VecHelper.getCoordinate(pos, barrelBlockAxis))
            return;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = barrelBlockAxis == Direction.Axis.X ? startPos.offset(0, xOffset, zOffset)
                        : barrelBlockAxis == Direction.Axis.Y ? startPos.offset(xOffset, 0, zOffset)
                        : startPos.offset(xOffset, zOffset, 0);
                BlockState blockState = world.getBlockState(offsetPos);
                if (blockState.getBlock() instanceof OilBarrelBlock)
                    continue;
                if (!blockState.getMaterial()
                        .isReplaceable())
                    return;
                tanksToPlace++;
            }
        }

        if (!player.isCreative() && stack.getCount() < tanksToPlace)
            return;

        for (int xOffset = 0; xOffset < width; xOffset++) {
            for (int zOffset = 0; zOffset < width; zOffset++) {
                BlockPos offsetPos = barrelBlockAxis == Direction.Axis.X ? startPos.offset(0, xOffset, zOffset)
                        : barrelBlockAxis == Direction.Axis.Y ? startPos.offset(xOffset, 0, zOffset)
                        : startPos.offset(xOffset, zOffset, 0);
                BlockState blockState = world.getBlockState(offsetPos);
                if (blockState.getBlock() instanceof OilBarrelBlock)
                    continue;
                BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
                player.getPersistentData()
                        .putBoolean("SilenceTankSound", true);
                super.place(context);
                player.getPersistentData()
                        .remove("SilenceTankSound");
            }
        }
    }
}
