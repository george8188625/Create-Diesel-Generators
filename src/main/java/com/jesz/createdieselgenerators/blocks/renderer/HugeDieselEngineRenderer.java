package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.entity.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PoweredEngineShaftBlockEntity;
import com.jozufozu.flywheel.backend.Backend;
import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import com.simibubi.create.foundation.render.SuperByteBuffer;
import com.simibubi.create.foundation.utility.AngleHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import static com.jesz.createdieselgenerators.blocks.HugeDieselEngineBlock.FACING;

public class HugeDieselEngineRenderer extends SafeBlockEntityRenderer<HugeDieselEngineBlockEntity> {
    public HugeDieselEngineRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    protected void renderSafe(HugeDieselEngineBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource bufferSource, int light, int overlay) {
        if (Backend.canUseInstancing(be.getLevel()))
            return;
        Float angle = be.getTargetAngle();
        VertexConsumer vb = bufferSource.getBuffer(RenderType.solid());
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(FACING);
        Direction.Axis facingAxis = facing.getAxis();
        if (angle == null){
            transformed(PartialModels.ENGINE_PISTON, state, facing, false)
                    .translate(0, 0.53475, 0)
                    .light(light)
                    .renderInto(ms, vb);
            return;
        }


        PoweredEngineShaftBlockEntity shaft = be.getShaft();
        if(shaft == null){
            transformed(PartialModels.ENGINE_PISTON, state, facing, false)
                    .translate(0, 0.53475, 0)
                    .light(light)
                    .renderInto(ms, vb);
            return;
        }
        Direction.Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);

        boolean roll90 = facingAxis.isHorizontal() && axis == Direction.Axis.Y || facingAxis.isVertical() && axis == Direction.Axis.Z;
        float shaftR = facing == Direction.DOWN ? -90 : facing == Direction.UP ? 90 : facing == Direction.WEST ? -90 : facing == Direction.EAST ? 90 : 0;
        if(roll90)
            shaftR = facing == Direction.NORTH ? 180 : facing == Direction.SOUTH ? 0 : facing == Direction.EAST ? -90 : facing == Direction.WEST ? 90 : 0;
        angle += (float)(shaftR*Math.PI/180);

        float sine = Mth.sin(angle) * (state.getValue(FACING).getAxis() == Direction.Axis.Y ? -1 : 1);
        float sine2 = Mth.sin(angle - Mth.HALF_PI) * (state.getValue(FACING).getAxis() == Direction.Axis.Y ? -1 : 1);
        float piston = ((1 - sine) / 4) + 0.4375f;

        transformed(PartialModels.ENGINE_PISTON, state, facing, roll90)
                .translate(0, piston, 0)
                .light(light)
                .renderInto(ms, vb);

        transformed(PartialModels.ENGINE_PISTON_LINKAGE, state, facing, roll90)
                .centre()
                .translate(0, 1, 0)
                .unCentre()
                .translate(0, piston, 0)
                .translate(0, 4 / 16f, 8 / 16f)
                .rotateX(sine2 * 23f)
                .translate(0, -4 / 16f, -8 / 16f)
                .light(light)
                .renderInto(ms, vb);
        if(shaft.isEngineForConnectorDisplay(be.getBlockPos()))
            transformed(PartialModels.ENGINE_PISTON_CONNECTOR, state, facing, roll90)
                    .translate(0, 2, 0)
                    .centre()
                    .rotateXRadians(-angle + Mth.HALF_PI)
                    .unCentre()
                    .light(light)
                    .renderInto(ms, vb);
    }
    private SuperByteBuffer transformed(PartialModel model, BlockState blockState, Direction facing, boolean roll90) {
        return CachedBufferer.partial(model, blockState)
                .centre()
                .rotateY(AngleHelper.horizontalAngle(facing))
                .rotateX(AngleHelper.verticalAngle(facing) + 90)
                .rotateY(roll90 ? -90 : 0)
                .unCentre();
    }
    @Override
    public int getViewDistance() {
        return 128;
    }
}
