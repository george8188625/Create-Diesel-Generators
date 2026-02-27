package com.jesz.createdieselgenerators.content.burner;

import com.jesz.createdieselgenerators.CDGBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BurnerBlock extends HorizontalAxisKineticBlock implements IBE<BurnerBlockEntity> {
    public static EnumProperty<BlazeBurnerBlock.HeatLevel> HEAT_LEVEL = BlazeBurnerBlock.HEAT_LEVEL;
    public static BooleanProperty LIT = BlockStateProperties.LIT;

    public BurnerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEAT_LEVEL, LIT);
        super.createBlockStateDefinition(builder);
    }

    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity)) {
            entity.hurt(level.damageSources().inFire(), 1);
        }

        super.entityInside(state, level, pos, entity);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(1, 0, 1, 15, 12, 15);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos otherPos, boolean moved) {
        if (level.getBlockEntity(pos) instanceof BurnerBlockEntity be)
            be.redstonePower = level.hasNeighborSignal(pos);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState p_60457_) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BurnerBlockEntity be)
            return be.redstoneOutput;

        return 0;
    }

    @Override
    public Class<BurnerBlockEntity> getBlockEntityClass() {
        return BurnerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BurnerBlockEntity> getBlockEntityType() {
        return CDGBlockEntityTypes.BURNER.get();
    }

}
