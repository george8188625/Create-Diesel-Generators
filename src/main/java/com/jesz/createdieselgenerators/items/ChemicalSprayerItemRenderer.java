package com.jesz.createdieselgenerators.items;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ChemicalSprayerItemRenderer extends CustomRenderedItemModelRenderer {
    protected static final PartialModel COG = new PartialModel(new ResourceLocation("createdieselgenerators:item/chemical_sprayer_cog"));

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);
        LocalPlayer player = Minecraft.getInstance().player;

        float worldTime = AnimationTickHolder.getRenderTime() / 10;
        float angle = worldTime * ((player.isUsingItem() && player.getItemInHand(player.getUsedItemHand()) == stack) ? -200 : -25);
        angle %= 360;

        ms.pushPose();
        ms.mulPose(Axis.ZP.rotationDegrees(angle));
        ms.translate(0.5, 0.5, 0.53125);
        renderer.render(COG.get(), light);
        ms.popPose();
    }
}
