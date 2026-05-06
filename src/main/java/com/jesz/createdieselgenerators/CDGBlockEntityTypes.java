package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlockEntity;
import com.jesz.createdieselgenerators.content.basin_lid.BasinLidRenderer;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlockEntity;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterRenderer;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.burner.BurnerRenderer;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterRenderer;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineInstance;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineRenderer;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.PoweredEngineShaftBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineRenderer;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlockEntity;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineRenderer;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlockEntity;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankRenderer;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlockEntity;
import com.jesz.createdieselgenerators.content.pumpjack.*;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlockEntity;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretRenderer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.kinetics.base.*;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

import java.util.ArrayList;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGBlockEntityTypes {


    public static final BlockEntityEntry<BurnerBlockEntity> BURNER = REGISTRATE.blockEntity("burner", BurnerBlockEntity::new)
            .visual(() -> ShaftVisual::new )
            .validBlocks(CDGBlocks.BURNER)
            .renderer(() -> BurnerRenderer::new)
            .register();

    public static final BlockEntityEntry<FluidPipeBlockEntity> CONCRETE_ENCASED_FLUID_PIPE = REGISTRATE.blockEntity("concrete_encased_fluid_pipe", FluidPipeBlockEntity::new)
            .validBlocksDeferred(() -> new ArrayList<>(CDGBlocks.CONCRETE_ENCASED_FLUID_PIPES.values()))
            .register();

    public static final BlockEntityEntry<ChemicalTurretBlockEntity> CHEMICAL_TURRET = REGISTRATE.blockEntity("chemical_turret", ChemicalTurretBlockEntity::new)
            .validBlocks(CDGBlocks.CHEMICAL_TURRET)
            .renderer(() -> ChemicalTurretRenderer::new)
            .register();

    public static final BlockEntityEntry<DieselEngineBlockEntity> DIESEL_ENGINE = REGISTRATE.blockEntity("diesel_engine_tile_entity", DieselEngineBlockEntity::new)
            .visual(() -> ShaftVisual::new)
            .validBlocks(CDGBlocks.DIESEL_ENGINE)
            .renderer(() -> DieselEngineRenderer::new)
            .register();

    public static final BlockEntityEntry<ModularDieselEngineBlockEntity> MODULAR_DIESEL_ENGINE = REGISTRATE.blockEntity("large_diesel_engine_tile_entity", ModularDieselEngineBlockEntity::new)
            .visual(() -> ShaftVisual::new )
            .validBlocks(CDGBlocks.MODULAR_DIESEL_ENGINE)
            .renderer(() -> ModularDieselEngineRenderer::new)
            .register();

    public static final BlockEntityEntry<HugeDieselEngineBlockEntity> HUGE_DIESEL_ENGINE = REGISTRATE.blockEntity("huge_diesel_engine_block_entity", HugeDieselEngineBlockEntity::new)
            .visual(() -> HugeDieselEngineInstance::new)
            .validBlocks(CDGBlocks.HUGE_DIESEL_ENGINE)
            .renderer(() -> HugeDieselEngineRenderer::new)
            .register();

    public static final BlockEntityEntry<PoweredEngineShaftBlockEntity> POWERED_ENGINE_SHAFT = REGISTRATE.blockEntity("powered_engine_shaft_block_entity", PoweredEngineShaftBlockEntity::new)
            .visual(() -> SingleAxisRotatingVisual.of(AllPartialModels.POWERED_SHAFT), false)
            .validBlocks(CDGBlocks.POWERED_ENGINE_SHAFT)
            .renderer(() -> KineticBlockEntityRenderer::new)
            .register();

    public static final BlockEntityEntry<BasinLidBlockEntity> BASIN_LID = REGISTRATE.blockEntity("basin_lid_tile_entity", BasinLidBlockEntity::new)
            .validBlocks(CDGBlocks.BASIN_LID)
            .renderer(() -> BasinLidRenderer::new)
            .register();

    public static final BlockEntityEntry<PumpjackBearingBlockEntity> PUMPJACK_BEARING = REGISTRATE.blockEntity("pumpjack_bearing_block_entity", PumpjackBearingBlockEntity::new)
            .visual(() -> NoShaftBearingInstance::new)
            .validBlocks(CDGBlocks.PUMPJACK_BEARING)
            .renderer(() -> NoShaftBearingRenderer::new)
            .register();

    public static final BlockEntityEntry<CanisterBlockEntity> CANISTER = REGISTRATE.blockEntity("canister_block_entity", CanisterBlockEntity::new)
            .validBlocks(CDGBlocks.CANISTER)
            .renderer(() -> CanisterRenderer::new)
            .register();

    public static final BlockEntityEntry<DistillationTankBlockEntity> DISTILLATION_TANK = REGISTRATE.blockEntity("distillation_tank_block_entity", DistillationTankBlockEntity::new)
            .validBlocks(CDGBlocks.DISTILLATION_TANK)
            .renderer(() -> DistillationTankRenderer::new)
            .register();

    public static final BlockEntityEntry<BulkFermenterBlockEntity> BULK_FERMENTER = REGISTRATE.blockEntity("bulk_fermenter", BulkFermenterBlockEntity::new)
            .validBlocks(CDGBlocks.BULK_FERMENTER)
            .renderer(() -> BulkFermenterRenderer::new)
            .register();

    public static final BlockEntityEntry<OilBarrelBlockEntity> OIL_BARREL = REGISTRATE.blockEntity("oil_barrel_block_entity", OilBarrelBlockEntity::new)
            .validBlocks(CDGBlocks.OIL_BARREL)
            .register();

    public static final BlockEntityEntry<PumpjackHoleBlockEntity> PUMPJACK_HOLE = REGISTRATE.blockEntity("pumpjack_hole_block_entity", PumpjackHoleBlockEntity::new)
            .displaySource(CDGDisplaySources.PUMPJACK_OIL_AMOUNT)
            .validBlocks(CDGBlocks.PUMPJACK_HOLE)
            .renderer(() -> PumpjackHoleRenderer::new)
            .register();

    public static final BlockEntityEntry<PumpjackCrankBlockEntity> PUMPJACK_CRANK = REGISTRATE.blockEntity("pumpjack_crank_block_entity", PumpjackCrankBlockEntity::new)
            .visual(() -> PumpjackCrankInstance::new)
            .validBlocks(CDGBlocks.PUMPJACK_CRANK)
            .renderer(() -> PumpjackCrankRenderer::new)
            .register();

    public static final BlockEntityEntry<KineticBlockEntity> ENCASED_GIRDER = REGISTRATE
            .blockEntity("encased_girder", KineticBlockEntity::new)
            .visual(() -> ShaftVisual::new, false)
            .validBlocks(CDGBlocks.ANDESITE_GIRDER_ENCASED_SHAFT)
            .renderer(() -> ShaftRenderer::new)
            .register();
    public static void register() {
    }
}
