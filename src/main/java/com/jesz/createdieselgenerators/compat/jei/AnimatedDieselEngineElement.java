package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.minecraft.core.Direction;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.POWERED;

public class AnimatedDieselEngineElement extends AnimatedKinetics {

    @Override
    public void draw(PoseStack matrixStack, int xOffset, int yOffset) {
        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 0);

        matrixStack.mulPose(Vector3f.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Vector3f.YP.rotationDegrees(22.5f + 90));
        int scale = 25;

        blockElement(shaft(Direction.Axis.X))
                .rotateBlock(-getCurrentAngle() * 6, 0, 0)
                .scale(scale)
                .render(matrixStack);

        blockElement(BlockRegistry.DIESEL_ENGINE.getDefaultState().setValue(POWERED, true))
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(matrixStack);

        matrixStack.popPose();
    }
}
