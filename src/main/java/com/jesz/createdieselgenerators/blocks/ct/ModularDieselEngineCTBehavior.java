package com.jesz.createdieselgenerators.blocks.ct;

import com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import static com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock.FACING;

public class ModularDieselEngineCTBehavior extends ConnectedTextureBehaviour {
    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
        return SpriteShifts.MODULAR_DIESEL_ENGINE;
    }

    @Override
    public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        return AllCTTypes.CROSS;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        if(!(state.getBlock() instanceof LargeDieselGeneratorBlock && other.getBlock() instanceof LargeDieselGeneratorBlock))
            return false;
        if(pos.relative(state.getValue(FACING)).equals(otherPos) || pos.relative(state.getValue(FACING).getOpposite()).equals(otherPos))
            return state.getValue(FACING).getAxis() == other.getValue(FACING).getAxis();
        return false;
    }
}
