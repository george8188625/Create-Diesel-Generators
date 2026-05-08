package com.jesz.createdieselgenerators.content.turret;

import com.jesz.createdieselgenerators.CDGPartialModels;
import com.jesz.createdieselgenerators.content.entity_filter.EntityFilteringRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ChemicalTurretRenderer extends KineticBlockEntityRenderer<ChemicalTurretBlockEntity> {
    public ChemicalTurretRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ChemicalTurretBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
        EntityFilteringRenderer.renderOnBlockEntity(be, partialTicks, ms, buffer, light, overlay);

        BlockState state = getRenderedBlockState(be);
        RenderType type = getRenderType(be, state);
        renderRotatingBuffer(be, getRotatedModel(be, state), ms, buffer.getBuffer(type), light);

        float horizontalRotation = AngleHelper.angleLerp(partialTicks, be.oldHorizontalRotation, be.horizontalRotation);
        float verticalRotation = AngleHelper.angleLerp(partialTicks, be.oldVerticalRotation, be.verticalRotation);

        CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_CONNECTOR, state)
                .center()
                .rotateYDegrees(horizontalRotation)
                .uncenter()
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_BODY, state)
                .center()
                .rotateYDegrees(horizontalRotation+180)
                .uncenter()
                .translate(0.5, 1.3125, 0.125)
                .rotateXDegrees(verticalRotation)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        if(be.lighterUpgrade)
            CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_LIGHTER, state)
                    .center()
                    .rotateYDegrees(horizontalRotation+180)
                    .uncenter()
                    .translate(0.5, 1.3125, 0.125)
                    .rotateXDegrees(verticalRotation)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderType.solid()));
        CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_SMALL_COG, state)
                .center()
                .rotateYDegrees(horizontalRotation+180)
                .uncenter()
                .translate(0.5, 1.3125, 0.125)
                .rotateXDegrees(verticalRotation)
                .rotateZDegrees(Mth.lerp(partialTicks, be.lastCogRotation, be.cogRotation))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected SuperByteBuffer getRotatedModel(ChemicalTurretBlockEntity be, BlockState state) {
        return CachedBuffers.partial(CDGPartialModels.CHEMICAL_TURRET_COG, state);
    }
}
