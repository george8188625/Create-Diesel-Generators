package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Components;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class EngineStateDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if(context.getSourceBlockEntity() instanceof DieselGeneratorBlockEntity sourceBE) {
            if(sourceBE.validFuel)
                return List.of(
                        Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                        Components.translatable("createdieselgenerators.display_source.speed").append(Math.abs(sourceBE.getGeneratedSpeed()) + "rpm"),
                        Components.translatable("createdieselgenerators.display_source.stress").append(Math.abs(sourceBE.calculateAddedStressCapacity() * sourceBE.getGeneratedSpeed()) + "su")
                );

            return List.of(
                    Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                    Components.translatable("createdieselgenerators.display_source.idle")
            );



        } else if(context.getSourceBlockEntity() instanceof LargeDieselGeneratorBlockEntity sourceBE) {
            if(sourceBE.FrontEngine != null)
                if(sourceBE.FrontEngine.validFuel)
                    return List.of(
                            Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                            Components.translatable("createdieselgenerators.display_source.speed").append(Math.abs(sourceBE.FrontEngine.getGeneratedSpeed()) + "rpm"),
                            Components.translatable("createdieselgenerators.display_source.stress").append(Math.abs(sourceBE.FrontEngine.calculateAddedStressCapacity() * sourceBE.FrontEngine.getGeneratedSpeed()) + "su")
                );

            return List.of(
                    Components.translatable("createdieselgenerators.display_source.engine_status").append(" : "),
                    Components.translatable("createdieselgenerators.display_source.idle")
            );

        }
            return List.of();
    }
}
