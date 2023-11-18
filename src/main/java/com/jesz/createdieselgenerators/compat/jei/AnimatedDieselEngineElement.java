package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;

public class AnimatedDieselEngineElement extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();

        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 100);

        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f + 90));
        int scale = 25;

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
                .render(graphics);

        blockElement(shaft(Direction.Axis.X))
                .rotateBlock(-getCurrentAngle() * 6, 0, 0)
                .scale(scale)
                        .render(graphics);
        blockElement(BlockRegistry.DIESEL_ENGINE.getDefaultState())
                .rotateBlock(0, 90, 0)
                .scale(scale)
                .render(graphics);

        matrixStack.popPose();
    }
}
