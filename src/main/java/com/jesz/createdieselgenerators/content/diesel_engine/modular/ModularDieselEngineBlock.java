package com.jesz.createdieselgenerators.content.diesel_engine.modular;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.jesz.createdieselgenerators.CDGBlocks;
import com.jesz.createdieselgenerators.CDGConfig;
import com.jesz.createdieselgenerators.content.diesel_engine.EngineUpgrades;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.placement.PoleHelper;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock.POWERED;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;

public class ModularDieselEngineBlock extends HorizontalKineticBlock implements IBE<ModularDieselEngineBlockEntity>, SpecialBlockItemRequirement {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final BooleanProperty PIPE = BooleanProperty.create("pipe");
    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public ModularDieselEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(super.defaultBlockState()
                .setValue(PIPE, true)
                .setValue(POWERED, false));
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() == state.getBlock() || isMoving)
            super.onPlace(state, level, pos, oldState, isMoving);
        withBlockEntityDo(level, pos, ModularDieselEngineBlockEntity::updateConnectivity);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moving) {
        level.setBlockAndUpdate(pos, state.setValue(POWERED, level.hasNeighborSignal(pos)));
        super.neighborChanged(state, level, pos, block, otherPos, moving);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (!player.isShiftKeyDown() && player.mayBuild()) {
            if (placementHelper.matchesItem(stack)) {
                placementHelper.getOffset(player, level, state, pos, hitResult)
                        .placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult);
                return ItemInteractionResult.SUCCESS;
            }
        }

        for (EngineUpgrades upgrade : EngineUpgrades.allUpgrades) {
            if (upgrade == EngineUpgrades.EMPTY)
                continue;
            if (upgrade.getItem().is(stack.getItem())) {
                withBlockEntityDo(level, pos, be -> {
                    ModularDieselEngineBlockEntity controller = be.getControllerBE();
                    if (controller == null || !upgrade.canAddOn(be) || controller.upgrade != EngineUpgrades.EMPTY)
                        return;

                    if (!player.isCreative())
                        stack.shrink(1);
                    be.upgrade = upgrade;
                    IWrenchable.playRotateSound(level, pos);
                    controller.sendData();
                });
                return ItemInteractionResult.SUCCESS;
            }
        }
        if(!CDGConfig.ENGINES_FILLED_WITH_ITEMS.get() || stack.isEmpty() || !(level.getBlockEntity(pos) instanceof SmartBlockEntity be))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
        if (tank == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (stack.getItem() instanceof BucketItem || stack.getItem() instanceof MilkBucketItem) {
            Fluid fluid = stack.getItem() instanceof BucketItem bi ? bi.content : NeoForgeMod.MILK.get();

            if (!tank.getFluidInTank(0).isEmpty())
                return ItemInteractionResult.FAIL;

            tank.fill(new FluidStack(fluid, 1000), IFluidHandler.FluidAction.EXECUTE);
            if (!player.isCreative())
                player.setItemInHand(hand, new ItemStack(Items.BUCKET));

            return ItemInteractionResult.SUCCESS;
        }

        IFluidHandlerItem itemTank = Capabilities.FluidHandler.ITEM.getCapability(stack, null);
        if (itemTank == null)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        itemTank.drain(tank.fill(itemTank.getFluidInTank(0), IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace() == Direction.UP) {
            KineticBlockEntity.switchToBlockState(context.getLevel(), context.getClickedPos(), updateAfterWrenched(state.setValue(PIPE, !state.getValue(PIPE)), context));
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
            return InteractionResult.SUCCESS;
        }

        withBlockEntityDo(context.getLevel(), context.getClickedPos(), be -> {
            ModularDieselEngineBlockEntity controller = be.getControllerBE();

            if (controller == null || controller.upgrade == EngineUpgrades.EMPTY || context.getLevel().isClientSide())
                return;

            if (!context.getPlayer().isCreative())
                context.getPlayer().getInventory().placeItemBackInInventory(controller.upgrade.getItem());

            controller.upgrade = EngineUpgrades.EMPTY;
            controller.sendData();
            IWrenchable.playRotateSound(context.getLevel(), context.getClickedPos());
        });
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        return originalState;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(PIPE, POWERED);
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock()) && !isMoving)
            withBlockEntityDo(level, pos, be -> {
                if (be.upgrade != EngineUpgrades.EMPTY)
                    popResource(level, pos, be.upgrade.getItem());
            });

        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity()) &&
                level.getBlockEntity(pos) instanceof ModularDieselEngineBlockEntity be) {
            level.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(be);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public Class<ModularDieselEngineBlockEntity> getBlockEntityClass() {
        return ModularDieselEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ModularDieselEngineBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.MODULAR_DIESEL_ENGINE.get();
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pState.getValue(FACING) == NORTH || pState.getValue(FACING) == SOUTH){
            return Shapes.or(Shapes.block(), Block.box(-2,0,0,18,4,16));
        }else{
            return Shapes.or(Shapes.block(), Block.box(0,0,-2,16,4,18));
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
        list.add(CDGBlocks.MODULAR_DIESEL_ENGINE.asStack());
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, list);
    }

    private static class PlacementHelper extends PoleHelper<Direction>{

        public PlacementHelper() {
            super(CDGBlocks.MODULAR_DIESEL_ENGINE::has, state -> state.getValue(FACING).getAxis(), FACING);
        }

        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return CDGBlocks.MODULAR_DIESEL_ENGINE::isIn;
        }
    }
}
