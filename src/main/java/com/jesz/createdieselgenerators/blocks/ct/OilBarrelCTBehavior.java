package com.jesz.createdieselgenerators.blocks.ct;

import com.jesz.createdieselgenerators.blocks.OilBarrelBlock;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTType;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import static com.jesz.createdieselgenerators.blocks.OilBarrelBlock.AXIS;
import static com.jesz.createdieselgenerators.blocks.OilBarrelBlock.OIL_BARREL_COLOR;

public class OilBarrelCTBehavior extends ConnectedTextureBehaviour {

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, TextureAtlasSprite sprite) {
        if(direction.getAxis() == state.getValue(AXIS))
            return SpriteShifts.OIL_BARREL_TOP;
        if((state.getValue(AXIS) == Direction.Axis.Z && direction.getAxis() == Direction.Axis.Y) || (state.getValue(AXIS) == Direction.Axis.Y)){
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.WHITE)
                return SpriteShifts.OIL_BARREL_WHITE;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.ORANGE)
                return SpriteShifts.OIL_BARREL_ORANGE;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.MAGENTA)
                return SpriteShifts.OIL_BARREL_MAGENTA;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_BLUE)
                return SpriteShifts.OIL_BARREL_LIGHT_BLUE;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.YELLOW)
                return SpriteShifts.OIL_BARREL_YELLOW;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIME)
                return SpriteShifts.OIL_BARREL_LIME;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PINK)
                return SpriteShifts.OIL_BARREL_PINK;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GRAY)
                return SpriteShifts.OIL_BARREL_GRAY;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_GRAY)
                return SpriteShifts.OIL_BARREL_LIGHT_GRAY;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.CYAN)
                return SpriteShifts.OIL_BARREL_CYAN;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PURPLE)
                return SpriteShifts.OIL_BARREL_PURPLE;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLUE)
                return SpriteShifts.OIL_BARREL_BLUE;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BROWN)
                return SpriteShifts.OIL_BARREL_BROWN;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GREEN)
                return SpriteShifts.OIL_BARREL_GREEN;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.RED)
                return SpriteShifts.OIL_BARREL_RED;
            if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLACK)
                return SpriteShifts.OIL_BARREL_BLACK;
            return SpriteShifts.OIL_BARREL;
        }
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.WHITE)
            return SpriteShifts.OIL_BARREL_SIDE_WHITE;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.ORANGE)
            return SpriteShifts.OIL_BARREL_SIDE_ORANGE;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.MAGENTA)
            return SpriteShifts.OIL_BARREL_SIDE_MAGENTA;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_BLUE)
            return SpriteShifts.OIL_BARREL_SIDE_LIGHT_BLUE;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.YELLOW)
            return SpriteShifts.OIL_BARREL_SIDE_YELLOW;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIME)
            return SpriteShifts.OIL_BARREL_SIDE_LIME;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PINK)
            return SpriteShifts.OIL_BARREL_SIDE_PINK;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GRAY)
            return SpriteShifts.OIL_BARREL_SIDE_GRAY;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.LIGHT_GRAY)
            return SpriteShifts.OIL_BARREL_SIDE_LIGHT_GRAY;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.CYAN)
            return SpriteShifts.OIL_BARREL_SIDE_CYAN;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.PURPLE)
            return SpriteShifts.OIL_BARREL_SIDE_PURPLE;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLUE)
            return SpriteShifts.OIL_BARREL_SIDE_BLUE;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BROWN)
            return SpriteShifts.OIL_BARREL_SIDE_BROWN;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.GREEN)
            return SpriteShifts.OIL_BARREL_SIDE_GREEN;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.RED)
            return SpriteShifts.OIL_BARREL_SIDE_RED;
        if(state.getValue(OIL_BARREL_COLOR) == OilBarrelBlock.OilBarrelColor.BLACK)
            return SpriteShifts.OIL_BARREL_SIDE_BLACK;
        return SpriteShifts.OIL_BARREL_SIDE;
    }

    @Override
    public CTType getDataType(BlockAndTintGetter world, BlockPos pos, BlockState state, Direction direction) {
        return AllCTTypes.RECTANGLE;
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face, Direction primaryOffset, Direction secondaryOffset) {
        return other.getBlock() instanceof OilBarrelBlock && ConnectivityHandler.isConnected(reader, pos, otherPos);
    }
}
