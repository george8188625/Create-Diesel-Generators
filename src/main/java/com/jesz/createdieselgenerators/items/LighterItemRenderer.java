package com.jesz.createdieselgenerators.items;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.PartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;

public class LighterItemRenderer extends CustomRenderedItemModelRenderer {
    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer, ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        try {
            if (stack.getTag() != null && CreateDieselGenerators.lighterSkins.containsKey(stack.getHoverName().getString().toLowerCase(Locale.ROOT))) {
                if (PartialModels.lighterSkinModels.containsKey(CreateDieselGenerators.lighterSkins.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT)))) {
                    if (stack.getTag().getInt("Type") == 1)
                        renderer.render(PartialModels.lighterSkinModels.get(CreateDieselGenerators.lighterSkins.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT))).getSecond().getFirst().get(), light);
                    else if (stack.getTag().getInt("Type") == 2)
                        renderer.render(PartialModels.lighterSkinModels.get(CreateDieselGenerators.lighterSkins.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT))).getSecond().getSecond().get(), light);
                    else
                        renderer.render(PartialModels.lighterSkinModels.get(CreateDieselGenerators.lighterSkins.get(stack.getHoverName().getString().toLowerCase(Locale.ROOT))).getFirst().get(), light);
                    return;
                }
            }
            if (stack.getTag() == null || stack.getTag().getInt("Type") == 0)
                renderer.render(PartialModels.lighterSkinModels.get("standard").getFirst().get(), light);
            else if (stack.getTag().getInt("Type") == 2)
                renderer.render(PartialModels.lighterSkinModels.get("standard").getSecond().getSecond().get(), light);
            else
                renderer.render(PartialModels.lighterSkinModels.get("standard").getSecond().getFirst().get(), light);
        }catch (NullPointerException e) {
            renderer.render(model.getOriginalModel(), light);
        }
    }
}
