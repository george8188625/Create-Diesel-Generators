package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.config.ConfigRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ISpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.placement.PoleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.jesz.createdieselgenerators.items.ItemRegistry.ENGINE_SILENCER;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class LargeDieselGeneratorBlock extends HorizontalKineticBlock implements IBE<LargeDieselGeneratorBlockEntity>, ISpecialBlockItemRequirement, ICDGKinetics {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    public static final BooleanProperty SILENCED = BooleanProperty.create("silenced");
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public LargeDieselGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState().setValue(PIPE, true));
        registerDefaultState(super.defaultBlockState().setValue(SILENCED, false));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        withBlockEntityDo(level, pos, be -> {
            if(be.getEngineBack() == null)
                be.updateStacked();
        });
        return super.updateShape(state, direction, neighbourState, level, pos, neighbourPos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        ItemStack itemInHand = player.getItemInHand(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(itemInHand)) {
                placementHelper.getOffset(player, level, state, pos, hit)
                        .placeInWorld(level, (BlockItem) itemInHand.getItem(), player, hand, hit);
                return InteractionResult.SUCCESS;
            }
        }

        if(ENGINE_SILENCER.isIn(itemInHand)) {
            if (state.getValue(SILENCED))
                return InteractionResult.PASS;
            if (!player.isCreative())
                itemInHand.shrink(1);
            level.setBlock(pos, state.setValue(SILENCED, true), 3);
            playRotateSound(level, pos);
            return InteractionResult.SUCCESS;
        }
        if(!ConfigRegistry.ENGINES_FILLED_WITH_ITEMS.get())
            return super.use(state, level, pos, player, hand, hit);
        if (itemInHand.isEmpty())
            return InteractionResult.PASS;
        if(level.getBlockEntity(pos) instanceof SmartBlockEntity be){
            IFluidHandler tank = be.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
            if(tank == null)
                return InteractionResult.PASS;
            if(itemInHand.getItem() instanceof BucketItem bi) {
                if (!tank.getFluidInTank(0).isEmpty())
                    return InteractionResult.FAIL;
                tank.fill(new FluidStack(bi.getFluid(), 1000), IFluidHandler.FluidAction.EXECUTE);
                if(!player.isCreative())
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
            if(itemInHand.getItem() instanceof MilkBucketItem) {
                if (!tank.getFluidInTank(0).isEmpty())
                    return InteractionResult.FAIL;
                tank.fill(new FluidStack(ForgeMod.MILK.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                if(!player.isCreative())
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                return InteractionResult.SUCCESS;
            }
            IFluidHandlerItem itemTank = itemInHand.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if(itemTank == null)
                return InteractionResult.PASS;
            itemTank.drain(tank.fill(itemTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        }
        return super.use(state, level, pos, player, hand, hit);
    }
    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if(state.getValue(SILENCED))
            if(context.getPlayer() != null && !context.getLevel().isClientSide) {
                if (!context.getPlayer().isCreative())
                    context.getPlayer().getInventory().placeItemBackInInventory(ENGINE_SILENCER.asStack());
                context.getLevel().setBlock(context.getClickedPos(), state.setValue(SILENCED, false), 3);
                playRotateSound(context.getLevel(), context.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        if(context.getClickedFace() == Direction.UP){
            KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), updateAfterWrenched(state.setValue(PIPE, !state.getValue(PIPE)), context));
            playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }
        return super.onWrenched(state,context);
    }
    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState;
    }
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(PIPE, SILENCED);
        super.createBlockStateDefinition(builder);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if(pContext.getPlayer().isShiftKeyDown())
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
        else
            return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
    @Override
    public Class<LargeDieselGeneratorBlockEntity> getBlockEntityClass() {
        return LargeDieselGeneratorBlockEntity.class;
    }
    @Override
    public BlockEntityType<? extends LargeDieselGeneratorBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.LARGE_DIESEL_ENGINE.get();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {

        if (pState.getValue(FACING) == NORTH || pState.getValue(FACING) == SOUTH){
            return Shapes.or(Block.box(0,0,0,16,16,16), Block.box(-2,0,0,18,4,16));
        }else{
            return Shapes.or(Block.box(0,0,0,16,16,16), Block.box(0,0,-2,16,4,18));
        }



    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return state.getValue(FACING)
                .getAxis() == face.getAxis();
    }
    @Override
    public Direction.Axis getRotationAxis(BlockState blockState) {
        return blockState.getValue(FACING)
                .getAxis();
    }

    @Override
    public ItemRequirement getRequiredItems(BlockState state, BlockEntity blockEntity) {
        List<ItemStack> list = new ArrayList<>();
        list.add(BlockRegistry.MODULAR_DIESEL_ENGINE.asStack());
        if(state.getValue(SILENCED))
            list.add(ItemRegistry.ENGINE_SILENCER.asStack());
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, list);
    }
    @Override
    public float getDefaultStressCapacity() {
        return 2048;
    }

    @Override
    public float getDefaultStressStressImpact() {
        return 0;
    }

    @Override
    public float getDefaultSpeed() {
        return 96;
    }

    private static class PlacementHelper extends PoleHelper<Direction>{

        public PlacementHelper() {
            super(BlockRegistry.MODULAR_DIESEL_ENGINE::has, state -> state.getValue(FACING).getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return BlockRegistry.MODULAR_DIESEL_ENGINE::isIn;
        }
    }
}
