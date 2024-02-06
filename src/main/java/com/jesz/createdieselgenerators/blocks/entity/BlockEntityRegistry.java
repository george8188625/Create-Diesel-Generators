package com.jesz.createdieselgenerators.blocks.entity;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.renderer.*;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import com.simibubi.create.content.kinetics.base.SingleRotatingInstance;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class BlockEntityRegistry {

    public static final BlockEntityEntry<DieselGeneratorBlockEntity> DIESEL_ENGINE = REGISTRATE.blockEntity("diesel_engine_tile_entity", DieselGeneratorBlockEntity::new)
            .instance(() -> ShaftInstance::new )
            .validBlocks(BlockRegistry.DIESEL_ENGINE)
            .renderer(() -> DieselGeneratorRenderer::new)
            .register();
    public static final BlockEntityEntry<LargeDieselGeneratorBlockEntity> LARGE_DIESEL_ENGINE = REGISTRATE.blockEntity("large_diesel_engine_tile_entity", LargeDieselGeneratorBlockEntity::new)
            .instance(() -> ShaftInstance::new )
            .validBlocks(BlockRegistry.MODULAR_DIESEL_ENGINE)
            .renderer(() -> LargeDieselGeneratorRenderer::new)
            .register();
    public static final BlockEntityEntry<HugeDieselEngineBlockEntity> HUGE_DIESEL_ENGINE = REGISTRATE.blockEntity("huge_diesel_engine_block_entity", HugeDieselEngineBlockEntity::new)
            .instance(() -> HugeDieselEngineInstance::new)
            .validBlocks(BlockRegistry.HUGE_DIESEL_ENGINE)
            .renderer(() -> HugeDieselEngineRenderer::new)
            .register();
    public static final BlockEntityEntry<PoweredEngineShaftBlockEntity> POWERED_ENGINE_SHAFT = REGISTRATE.blockEntity("powered_engine_shaft_block_entity", PoweredEngineShaftBlockEntity::new)
            .instance(() -> SingleRotatingInstance::new, false)
            .validBlocks(BlockRegistry.POWERED_ENGINE_SHAFT)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();
    public static final BlockEntityEntry<BasinLidBlockEntity> BASIN_LID = REGISTRATE.blockEntity("basin_lid_tile_entity", BasinLidBlockEntity::new)
            .validBlocks(BlockRegistry.BASIN_LID)
            .renderer(() -> BasinLidRenderer::new)
            .register();
    public static final BlockEntityEntry<PumpjackBearingBlockEntity> PUMPJACK_BEARING = REGISTRATE.blockEntity("pumpjack_bearing_block_entity", PumpjackBearingBlockEntity::new)
            .instance(() -> NoShaftBearingInstance::new)
            .validBlocks(BlockRegistry.PUMPJACK_BEARING)
            .renderer(() -> NoShaftBearingRenderer::new)
            .register();
    public static final BlockEntityEntry<CanisterBlockEntity> CANISTER = REGISTRATE.blockEntity("canister_block_entity", CanisterBlockEntity::new)
            .validBlocks(BlockRegistry.CANISTER)
            .register();
    public static final BlockEntityEntry<DistillationTankBlockEntity> DISTILLATION_TANK = REGISTRATE.blockEntity("distillation_tank_block_entity", DistillationTankBlockEntity::new)
            .validBlocks(BlockRegistry.DISTILLATION_TANK)
            .renderer(() -> DistillationTankRenderer::new)
            .register();
    public static final BlockEntityEntry<OilBarrelBlockEntity> OIL_BARREL = REGISTRATE.blockEntity("oil_barrel_block_entity", OilBarrelBlockEntity::new)
            .validBlocks(BlockRegistry.OIL_BARREL)
            .register();
    public static final BlockEntityEntry<PumpjackHoleBlockEntity> PUMPJACK_HOLE = REGISTRATE.blockEntity("pumpjack_hole_block_entity", PumpjackHoleBlockEntity::new)
            .validBlocks(BlockRegistry.PUMPJACK_HOLE)
            .renderer(() -> PumpjackHoleRenderer::new)
            .register();
    public static final BlockEntityEntry<PumpjackCrankBlockEntity> PUMPJACK_CRANK = REGISTRATE.blockEntity("pumpjack_crank_block_entity", PumpjackCrankBlockEntity::new)
            .instance(() -> PumpjackCrankInstance::new)
            .validBlocks(BlockRegistry.PUMPJACK_CRANK)
            .renderer(() -> PumpjackCrankRenderer::new)
            .register();

    public static void register() {
    }
}
