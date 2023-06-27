package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class BlockEntityRegistry {

    public static final BlockEntityEntry<DieselGeneratorBlockEntity> DIESEL_ENGINE = REGISTRATE.blockEntity("diesel_engine_tile_entity", DieselGeneratorBlockEntity::new)
            .instance(() -> ShaftInstance::new, false)
            .validBlocks(BlockRegistry.DIESEL_ENGINE)
            .renderer(() -> DieselGeneratorRenderer::new)
            .register();
    public static final BlockEntityEntry<LargeDieselGeneratorBlockEntity> LARGE_DIESEL_ENGINE = REGISTRATE.blockEntity("large_diesel_engine_tile_entity", LargeDieselGeneratorBlockEntity::new)
            .instance(() -> ShaftInstance::new, false)
            .validBlocks(BlockRegistry.MODULAR_DIESEL_ENGINE)
            .renderer(() -> LargeDieselGeneratorRenderer::new)
            .register();
    public static final BlockEntityEntry<BasinLidBlockEntity> BASIN_LID = REGISTRATE.blockEntity("basin_lid_tile_entity", BasinLidBlockEntity::new)
            .validBlocks(BlockRegistry.BASIN_LID)
            .register();

    public static void register() {
    }
}
