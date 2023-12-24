package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import com.simibubi.create.foundation.render.CachedBufferer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.FACING;
import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.TURBOCHARGED;

public class DieselGeneratorRenderer extends ShaftRenderer<DieselGeneratorBlockEntity> {

    public DieselGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(DieselGeneratorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        int angle = (int) (Math.abs(KineticBlockEntityRenderer.getAngleForTe(be, be.getBlockPos(), KineticBlockEntityRenderer.getRotationAxisOf(be))*180/Math.PI) * 3 % 360)/36;
        if(!be.getBlockState().getValue(TURBOCHARGED))
            if(be.getBlockState().getValue(FACING).getAxis().isHorizontal()){
                CachedBufferer.partial( angle == 10? PartialModels.ENGINE_PISTONS_0 :
                                        angle == 9 ? PartialModels.ENGINE_PISTONS_1 :
                                        angle == 8 ? PartialModels.ENGINE_PISTONS_2 :
                                        angle == 7 ? PartialModels.ENGINE_PISTONS_3 :
                                        angle == 6 ? PartialModels.ENGINE_PISTONS_4 :
                                        angle == 5 ? PartialModels.ENGINE_PISTONS_4 :
                                        angle == 4 ? PartialModels.ENGINE_PISTONS_3 :
                                        angle == 3 ? PartialModels.ENGINE_PISTONS_2 :
                                        angle == 2 ? PartialModels.ENGINE_PISTONS_1 :
                                                PartialModels.ENGINE_PISTONS_0
                        , be.getBlockState()).centre()
                        .rotateY(be.getBlockState().getValue(FACING).toYRot()).unCentre()
                        .light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
            }else {
                 CachedBufferer.partial(angle == 10? PartialModels.ENGINE_PISTONS_VERTICAL_0 :
                                         angle == 9 ? PartialModels.ENGINE_PISTONS_VERTICAL_1 :
                                         angle == 8 ? PartialModels.ENGINE_PISTONS_VERTICAL_2 :
                                         angle == 7 ? PartialModels.ENGINE_PISTONS_VERTICAL_3 :
                                         angle == 6 ? PartialModels.ENGINE_PISTONS_VERTICAL_4 :
                                         angle == 5 ? PartialModels.ENGINE_PISTONS_VERTICAL_4 :
                                         angle == 4 ? PartialModels.ENGINE_PISTONS_VERTICAL_3 :
                                         angle == 3 ? PartialModels.ENGINE_PISTONS_VERTICAL_2 :
                                         angle == 2 ? PartialModels.ENGINE_PISTONS_VERTICAL_1 :
                                                 PartialModels.ENGINE_PISTONS_VERTICAL_0
                                , be.getBlockState()).centre().rotateY(be.getBlockState().getValue(FACING) == Direction.DOWN ? 180 : 270).rotateZ(be.getBlockState().getValue(FACING) == Direction.DOWN ? 180 : 0).unCentre()
                         .light(light).renderInto(ms, buffer.getBuffer(RenderType.solid()));
            }

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
    }
}
