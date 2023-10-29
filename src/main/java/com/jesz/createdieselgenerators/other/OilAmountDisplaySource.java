package com.jesz.createdieselgenerators.other;

import com.jesz.createdieselgenerators.blocks.entity.DieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.LargeDieselGeneratorBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackHoleBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.DisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.utility.Lang;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.translate;

public class OilAmountDisplaySource extends DisplaySource {
    @Override
    public List<MutableComponent> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if(context.getSourceBlockEntity() instanceof PumpjackHoleBlockEntity sourceBE) {
            return List.of(
                    translate("createdieselgenerators.display_source.pumpjack_hole_source").append(" : "),
                    Lang.number(sourceBE.oilAmount).add(Lang.translate("generic.unit.buckets")).component()
            );

        }
        return List.of();
    }

}
