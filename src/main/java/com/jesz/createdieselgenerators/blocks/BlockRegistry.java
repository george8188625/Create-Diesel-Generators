package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.CreativeTab;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MaterialColor;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class BlockRegistry {
static {
        REGISTRATE.creativeModeTab(() -> CreativeTab.CREATIVE_TAB);
    }
public static final BlockEntry<DieselGeneratorBlock> DIESEL_ENGINE = REGISTRATE.block("diesel_engine", DieselGeneratorBlock::new)
            .properties(p -> p.color(MaterialColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .item()
            .transform(customItemModel("_", "block"))
            .register();
    public static final BlockEntry<LargeDieselGeneratorBlock> MODULAR_DIESEL_ENGINE = REGISTRATE.block("large_diesel_engine", LargeDieselGeneratorBlock::new)
            .properties(p -> p.color(MaterialColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .item()
            .transform(customItemModel("_", "block"))
            .register();
    public static final BlockEntry<BasinLidBlock> BASIN_LID = REGISTRATE.block("basin_lid", BasinLidBlock::new)
            .properties(p -> p.color(MaterialColor.COLOR_BLACK))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .blockstate(BlockStateGen.horizontalBlockProvider(true))
            .item()
            .transform(customItemModel("_", "block"))
            .register();

    public static void register() {
    }
}
