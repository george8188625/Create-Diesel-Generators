package com.jesz.createdieselgenerators.blocks.renderer;

import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.simibubi.create.content.kinetics.base.ShaftRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DieselGeneratorRenderer extends ShaftRenderer<DieselGeneratorBlockEntity> {

    public DieselGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

}
