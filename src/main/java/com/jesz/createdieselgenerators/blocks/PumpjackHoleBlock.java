package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class PumpjackHoleBlock extends Block implements IBE<PumpjackHoleBlockEntity>, IWrenchable {
    public PumpjackHoleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(NORTH, true)
                .setValue(SOUTH, true)
                .setValue(WEST, false)
                .setValue(EAST, false));
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(context.getClickedFace().getAxis().isHorizontal()){
            context.getLevel().setBlock(context.getClickedPos(), state.setValue(BooleanProperty.create(context.getClickedFace().getName()), !state.getValue(BooleanProperty.create(context.getClickedFace().getName()))), 3);
            playRotateSound(context.getLevel(), context.getClickedPos());

            return InteractionResult.SUCCESS;
        }
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public Class<PumpjackHoleBlockEntity> getBlockEntityClass() {
        return PumpjackHoleBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpjackHoleBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.PUMPJACK_HOLE.get();
    }
}
