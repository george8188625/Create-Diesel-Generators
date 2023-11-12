package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.minecraft.core.Direction;

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
        int angle = (int) (getCurrentAngle() * 18 % 360)/36;

        blockElement(angle == 10? PartialModels.ENGINE_PISTONS_0 :
                    angle == 9 ? PartialModels.ENGINE_PISTONS_1 :
                    angle == 8 ? PartialModels.ENGINE_PISTONS_2 :
                    angle == 7 ? PartialModels.ENGINE_PISTONS_3 :
                    angle == 6 ? PartialModels.ENGINE_PISTONS_4 :
                    angle == 5 ? PartialModels.ENGINE_PISTONS_4 :
                    angle == 4 ? PartialModels.ENGINE_PISTONS_3 :
                    angle == 3 ? PartialModels.ENGINE_PISTONS_2 :
                    angle == 2 ? PartialModels.ENGINE_PISTONS_1 :
                            PartialModels.ENGINE_PISTONS_0)
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(matrixStack);

        blockElement(BlockRegistry.DIESEL_ENGINE.getDefaultState())
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(matrixStack);

        matrixStack.popPose();
    }
}
