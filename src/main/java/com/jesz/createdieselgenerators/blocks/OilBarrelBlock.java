package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.entity.BlockEntityRegistry;
import com.jesz.createdieselgenerators.blocks.entity.OilBarrelBlockEntity;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.ForgeSoundType;

import java.util.Locale;

public class OilBarrelBlock extends Block implements IBE<OilBarrelBlockEntity>, IWrenchable {

    public static final EnumProperty<OilBarrelColor> OIL_BARREL_COLOR = EnumProperty.create("color", OilBarrelColor.class);
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public OilBarrelBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(OIL_BARREL_COLOR, OilBarrelColor.NONE));
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        withBlockEntityDo(world, pos, OilBarrelBlockEntity::updateConnectivity);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos().relative(context.getClickedFace().getOpposite()));
        if(state.getBlock() instanceof OilBarrelBlock && !context.getPlayer().isShiftKeyDown())
            return defaultBlockState().setValue(AXIS, state.getValue(AXIS));
        return defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OIL_BARREL_COLOR, AXIS);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof OilBarrelBlockEntity))
                return;
            OilBarrelBlockEntity tankBE = (OilBarrelBlockEntity) be;
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(tankBE);
        }
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace().getAxis() != state.getValue(AXIS)) {
            BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
            if (be instanceof OilBarrelBlockEntity tankBE) {
                context.getLevel().removeBlockEntity(context.getClickedPos());
                ConnectivityHandler.splitMulti(tankBE);
            }
        }
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if(stackInHand.is(Tags.Items.DYES_WHITE)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.WHITE);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_ORANGE)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.ORANGE);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_MAGENTA)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.MAGENTA);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_LIGHT_BLUE)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.LIGHT_BLUE);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_YELLOW)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.YELLOW);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_LIME)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.LIME);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_PINK)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.PINK);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_GRAY)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.GRAY);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_LIGHT_GRAY)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.LIGHT_GRAY);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_CYAN)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.CYAN);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_PURPLE)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.PURPLE);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_BLUE)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.BLUE);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_BROWN)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.BROWN);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_GREEN)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.GREEN);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_RED)){
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.RED);
            if(r != null)
                return r;
        }if(stackInHand.is(Tags.Items.DYES_BLACK)) {
            InteractionResult r = tryDye(state, level, pos, player, stackInHand, OilBarrelColor.BLACK);
            if(r != null)
                return r;
        }
        return super.use(state, level, pos, player, hand, hit);

    }

    public InteractionResult tryDye(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack, OilBarrelColor color){
        if(state.getValue(OIL_BARREL_COLOR) == color){
            if(level.getBlockEntity(pos) instanceof OilBarrelBlockEntity be){
                OilBarrelBlockEntity controllerBE = be.getControllerBE();
                if(controllerBE != null){
                    boolean successful = false;
                    for (int x = 0; x < controllerBE.getWidth(); x++) {
                        for (int z = 0; z < controllerBE.getWidth(); z++) {
                            BlockPos offsetPos = state.getValue(AXIS) == Direction.Axis.X ? new BlockPos(pos.getX(), be.getController().getY()+x, be.getController().getZ()+z)
                                    : state.getValue(AXIS) == Direction.Axis.Y ? be.getController().offset(x, 0, z).atY(pos.getY())
                                    : new BlockPos(be.getController().getX()+x, be.getController().getY()+z, pos.getZ());
                            BlockState blockState = level.getBlockState(offsetPos);
                            if(blockState.getBlock() instanceof OilBarrelBlock && !stack.isEmpty()){
                                if(blockState.getValue(OIL_BARREL_COLOR) == color)
                                    continue;
                                level.setBlock(offsetPos, state.setValue(OIL_BARREL_COLOR, color), 2);
                                if(!player.isCreative())
                                    stack.shrink(1);
                                successful = true;
                            }
                        }
                    }
                    if(successful)
                        return InteractionResult.SUCCESS;
                }
            }
        }else{
            level.setBlock(pos, state.setValue(OIL_BARREL_COLOR, color), 2);
            if(!player.isCreative())
                stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    @Override
    public Class<OilBarrelBlockEntity> getBlockEntityClass() {
        return OilBarrelBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends OilBarrelBlockEntity> getBlockEntityType() {
        return BlockEntityRegistry.OIL_BARREL.get();
    }

    // Tanks are less noisy when placed in batch
    public static final SoundType SILENCED_METAL =
            new ForgeSoundType(0.1F, 1.5F, () -> SoundEvents.METAL_BREAK, () -> SoundEvents.METAL_STEP,
                    () -> SoundEvents.METAL_PLACE, () -> SoundEvents.METAL_HIT, () -> SoundEvents.METAL_FALL);

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        SoundType soundType = super.getSoundType(state, world, pos, entity);
        if (entity != null && entity.getPersistentData()
                .contains("SilenceTankSound"))
            return SILENCED_METAL;
        return soundType;
    }

    public enum OilBarrelColor implements StringRepresentable{
        WHITE,
        ORANGE,
        MAGENTA,
        LIGHT_BLUE,
        YELLOW,
        LIME,
        PINK,
        GRAY,
        LIGHT_GRAY,
        CYAN,
        PURPLE,
        BLUE,
        BROWN,
        GREEN,
        RED,
        BLACK,
        NONE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
