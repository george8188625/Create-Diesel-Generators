package com.jesz.createdieselgenerators.content.andesite_girder;

import com.jesz.createdieselgenerators.CDGBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.decoration.girder.GirderBlock;
import com.simibubi.create.content.decoration.girder.GirderEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class AndesiteGirderBlock extends GirderBlock {
    private static final int placementHelperId = PlacementHelpers.register(new AndesiteGirderPlacementHelper());

    public AndesiteGirderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState p_60578_, BlockGetter p_60579_, BlockPos p_60580_) {
        return Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (player == null)
            return InteractionResult.PASS;

        ItemStack itemInHand = player.getItemInHand(hand);
        if (AllBlocks.SHAFT.isIn(itemInHand)) {
            KineticBlockEntity.switchToBlockState(level, pos, CDGBlocks.ANDESITE_GIRDER_ENCASED_SHAFT.getDefaultState()
                    .setValue(WATERLOGGED, state.getValue(WATERLOGGED))
                    .setValue(TOP, state.getValue(TOP))
                    .setValue(BOTTOM, state.getValue(BOTTOM))
                    .setValue(GirderEncasedShaftBlock.HORIZONTAL_AXIS, state.getValue(X) || hit.getDirection()
                            .getAxis() == Direction.Axis.Z ? Direction.Axis.Z : Direction.Axis.X));

            level.playSound(null, pos, SoundEvents.NETHERITE_BLOCK_HIT, SoundSource.BLOCKS, 0.5f, 1.25f);
            if (!level.isClientSide && !player.isCreative()) {
                itemInHand.shrink(1);
                if (itemInHand.isEmpty())
                    player.setItemInHand(hand, ItemStack.EMPTY);
            }
        }

        IPlacementHelper helper = PlacementHelpers.get(placementHelperId);
        if (helper.matchesItem(itemInHand))
            return helper.getOffset(player, level, state, pos, hit)
                    .placeInWorld(level, (BlockItem) itemInHand.getItem(), player, hand, hit);
        if (AllItems.WRENCH.isIn(itemInHand) && !player.isSteppingCarefully()) {
            if (AndesiteGirderWrenchBehaviour.handleClick(level, pos, state, hit))
                return InteractionResult.sidedSuccess(level.isClientSide);
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }


}
