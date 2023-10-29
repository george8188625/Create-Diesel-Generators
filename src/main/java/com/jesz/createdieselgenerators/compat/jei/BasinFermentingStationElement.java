package com.jesz.createdieselgenerators.compat.jei;

import com.jesz.createdieselgenerators.PartialModels;
import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;

import static com.jesz.createdieselgenerators.blocks.BasinLidBlock.ON_A_BASIN;

public class BasinFermentingStationElement extends AnimatedKinetics {

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        PoseStack matrixStack = graphics.pose();

        matrixStack.pushPose();
        matrixStack.translate(xOffset, yOffset, 200);
        matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
        matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        int scale = 23;

        GuiGameElement.of(BlockRegistry.BASIN_LID.getDefaultState().setValue(ON_A_BASIN, true))
                .atLocal(0, 0, 0)
                .scale(scale)
                .render(graphics);

        GuiGameElement.of(AllBlocks.BASIN.getDefaultState())
                .atLocal(0, 1, 0)
                .scale(scale)
                .render(graphics);
        blockElement(PartialModels.SMALL_GAUGE_DIAL).atLocal(0.5625f, 0.375, 0.5625f)
                .scale(scale)
                .rotate(0, 0, getCurrentAngle()/4)
                .render(graphics);

        matrixStack.popPose();
    }
}
