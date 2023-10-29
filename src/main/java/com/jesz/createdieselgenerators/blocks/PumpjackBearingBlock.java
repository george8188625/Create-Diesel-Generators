package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class PumpjackBearingBlock extends BearingBlock implements IBE<PumpjackBearingBlockEntity> {
    public PumpjackBearingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
                                 BlockHitResult hit) {
        if (!player.mayBuild())
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return InteractionResult.FAIL;
        if (player.getItemInHand(handIn)
                .isEmpty()) {
            if (worldIn.isClientSide)
                return InteractionResult.SUCCESS;
            withBlockEntityDo(worldIn, pos, be -> {
                if (be.isRunning()) {
                    be.disassemble();
                    return;
                }
                be.assembleNextTick();
            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }
    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        context.getLevel().setBlock(context.getClickedPos(), BlockRegistry.PUMPJACK_BEARING_B.getDefaultState().setValue(PumpjackBearingBBlock.FACING, state.getValue(FACING).getAxis() != Direction.Axis.Y ? state.getValue(FACING) : Direction.NORTH), 2);

        return InteractionResult.SUCCESS;
    }
    @Override
    public Class<PumpjackBearingBlockEntity> getBlockEntityClass() {
        return PumpjackBearingBlockEntity.class;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = context.getHorizontalDirection();
        if(context.getPlayer().isShiftKeyDown())
            return defaultBlockState().setValue(FACING, preferred.getOpposite());
        return defaultBlockState().setValue(FACING, preferred);
    }
    @Override
    public BlockEntityType<? extends PumpjackBearingBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.PUMPJACK_BEARING.get();
    }
}
