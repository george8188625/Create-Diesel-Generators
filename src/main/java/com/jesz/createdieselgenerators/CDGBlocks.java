package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderBlock;
import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderEncasedShaftBlock;
import com.jesz.createdieselgenerators.content.andesite_girder.AndesiteGirderGenerator;
import com.jesz.createdieselgenerators.content.basin_lid.BasinLidBlock;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterBlock;
import com.jesz.createdieselgenerators.content.bulk_fermenter.BulkFermenterCTBehavior;
import com.jesz.createdieselgenerators.content.burner.BurnerBlock;
import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.jesz.createdieselgenerators.content.canister.CanisterBlock;
import com.jesz.createdieselgenerators.content.canister.CanisterBlockItem;
import com.jesz.createdieselgenerators.content.concrete.ConcreteEncasedFluidPipeBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.HugeDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.huge.PoweredEngineShaftBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineBlock;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineCTBehavior;
import com.jesz.createdieselgenerators.content.diesel_engine.modular.ModularDieselEngineGenerator;
import com.jesz.createdieselgenerators.content.diesel_engine.normal.DieselEngineBlock;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankBlock;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankGenerator;
import com.jesz.createdieselgenerators.content.distillation.DistillationTankModel;
import com.jesz.createdieselgenerators.content.items.MultiBlockContainerBlockItem;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelBlock;
import com.jesz.createdieselgenerators.content.oil_barrel.OilBarrelCTBehavior;
import com.jesz.createdieselgenerators.content.pumpjack.*;
import com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelBlock;
import com.jesz.createdieselgenerators.content.sheetmetal.SheetMetalPanelModel;
import com.jesz.createdieselgenerators.content.turret.ChemicalTurretBlock;
import com.jesz.createdieselgenerators.contraption.DieselEngineMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackBearingBMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackHeadMovementBehaviour;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.boiler.BoilerHeater;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.TagGen.axeOnly;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class CDGBlocks {
    
    public static final BlockEntry<BurnerBlock> BURNER = REGISTRATE.block("burner", BurnerBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(pickaxeOnly())
            .tag(CDGTags.HEAT_SOURCES)
            .blockstate((c, p) -> BlockStateGen.horizontalAxisBlock(c, p, bs -> AssetLookup.partialBaseModel(c, p)))
            .onRegister((b) -> BoilerHeater.REGISTRY.register(b, ((level, pos, state) -> {
                if(level.getBlockEntity(pos) instanceof BurnerBlockEntity be)
                    return state.getValue(BurnerBlock.LIT) ? be.heat : -1;
                return -1;
            })))
            .item().model((c, p) -> p.blockItem(c, "/item")).build()
            .register();

    public static final BlockEntry<ChemicalTurretBlock> CHEMICAL_TURRET = REGISTRATE.block("chemical_turret", ChemicalTurretBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
            .onRegister(b -> BlockStressValues.IMPACTS.register(b, () -> 4))
            .item().model((c, p) -> p.blockItem(c, "/item")).build()
            .register();

    public static final BlockEntry<DieselEngineBlock> DIESEL_ENGINE = REGISTRATE.block("diesel_engine", DieselEngineBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .transform(pickaxeOnly())
            .blockstate((c, p) ->
                    p.getVariantBuilder(c.getEntry())
                            .forAllStates(bs ->
                                    ConfiguredModel.builder()
                                            .modelFile(AssetLookup.partialBaseModel(c, p, bs.getValue(DieselEngineBlock.FACING).getAxis().isVertical() ? "vertical" : ""))
                                            .rotationY(bs.getValue(DieselEngineBlock.FACING).getAxis().isVertical() ? (bs.getValue(DieselEngineBlock.FACING) == Direction.UP ? 90 : 180) : (int) bs.getValue(DieselEngineBlock.FACING).toYRot())
                                            .build()
                            )
            )
            .onRegister(movementBehaviour(new DieselEngineMovementBehaviour()))
            .item()
            .tag(AllTags.AllItemTags.CONTRAPTION_CONTROLLED.tag)
            .model((c, p) -> p.blockItem(c, "/item"))
            .build()
            .register();


    public static final BlockEntry<ModularDieselEngineBlock> MODULAR_DIESEL_ENGINE = REGISTRATE.block("large_diesel_engine", ModularDieselEngineBlock::new)
            .lang("Modular Diesel Engine")
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .transform(pickaxeOnly())
            .blockstate(new ModularDieselEngineGenerator()::generate)
            .onRegister(connectedTextures(ModularDieselEngineCTBehavior::new))
            .onRegister(movementBehaviour(new DieselEngineMovementBehaviour()))
            .item().model((c, p) -> p.withExistingParent("large_diesel_engine", p.modLoc("block/modular_diesel_engine/item"))).build()
            .register();

    public static final BlockEntry<HugeDieselEngineBlock> HUGE_DIESEL_ENGINE = REGISTRATE.block("huge_diesel_engine", HugeDieselEngineBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .properties(p -> p.noOcclusion())
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
            .item().model((c, p) -> p.blockItem(c, "/item")).build()
            .register();

    public static final BlockEntry<PoweredEngineShaftBlock> POWERED_ENGINE_SHAFT = REGISTRATE.block("powered_engine_shaft", PoweredEngineShaftBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(pickaxeOnly())
            .loot((p, b) -> p.dropOther(b, AllBlocks.SHAFT))
            .blockstate(BlockStateGen.axisBlockProvider(false))
            .register();

    public static final BlockEntry<BasinLidBlock> BASIN_LID = REGISTRATE.block("basin_lid", BasinLidBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .transform(pickaxeOnly())
            .blockstate((c, p) ->
                    p.getVariantBuilder(c.getEntry())
                            .forAllStates(bs ->
                                 ConfiguredModel.builder()
                                        .modelFile(AssetLookup.partialBaseModel(c, p, bs.getValue(BasinLidBlock.ON_A_BASIN) ? (bs.getValue(BasinLidBlock.OPEN) ? "on_a_basin_open" : "on_a_basin") : (bs.getValue(BasinLidBlock.OPEN) ? "open" : "")))
                                        .rotationY((int) bs.getValue(BasinLidBlock.FACING).toYRot())
                                        .build()
                            )
            )
            .item().model((c, p) -> p.blockItem(c, "/block")).build()
            .register();

    public static final BlockEntry<PumpjackBearingBlock> PUMPJACK_BEARING = REGISTRATE.block("pumpjack_bearing", PumpjackBearingBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.GLOW_LICHEN))
            .properties(p -> p.noOcclusion())
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.directionalBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .item().model((c, p) -> p.cubeBottomTop("pumpjack_bearing", p.modLoc("block/pumpjack_bearing_side"), p.modLoc("block/pumpjack_bearing_back"), p.modLoc("block/pumpjack_bearing_top"))).build()
            .register();

    public static final BlockEntry<PumpjackHeadBlock> PUMPJACK_HEAD = REGISTRATE.block("pumpjack_head", PumpjackHeadBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.GLOW_LICHEN))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.standardModel(c, p), 0))
            .onRegister(movementBehaviour(new PumpjackHeadMovementBehaviour()))
            .simpleItem()
            .register();

    public static final BlockEntry<PumpjackBearingBBlock> PUMPJACK_BEARING_B = REGISTRATE.block("pumpjack_bearing_b", PumpjackBearingBBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.GLOW_LICHEN))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.standardModel(c, p), 90))
            .loot((p, b) -> p.dropOther(b, PUMPJACK_BEARING.get()))
            .onRegister(movementBehaviour(new PumpjackBearingBMovementBehaviour()))
            .register();

    public static final BlockEntry<PumpjackHoleBlock> PUMPJACK_HOLE = REGISTRATE.block("pumpjack_hole", PumpjackHoleBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .transform(pickaxeOnly())
            .blockstate(PumpjackHoleGenerator::blockState)
            .item().model((c, p) -> p.cubeBottomTop("pumpjack_hole", p.modLoc("block/pumpjack_hole_pipe"), p.modLoc("block/pumpjack_hole_base"), p.modLoc("block/pumpjack_hole_pipe"))).build()
            .register();

    public static final BlockEntry<PumpjackCrankBlock> PUMPJACK_CRANK = REGISTRATE.block("pumpjack_crank", PumpjackCrankBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.GLOW_LICHEN))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.horizontalBlock(c.getEntry(), AssetLookup.partialBaseModel(c, p)))
            .item().model((c, p) -> p.blockItem(c, "/item")).build()
            .register();

    public static final BlockEntry<CanisterBlock> CANISTER = REGISTRATE.block("canister", CanisterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.getVariantBuilder(c.getEntry())
                    .forAllStates(state -> {
                        Direction facing = state.getValue(CanisterBlock.FACING);
                        return ConfiguredModel.builder()
                                .modelFile(AssetLookup.partialBaseModel(c, p, facing.getAxis().isVertical() ? "" : "horizontal"))
                                .rotationY(facing.getAxis().isVertical() ? 0 : (int) facing.getOpposite().toYRot())
                                .rotationX(facing.getAxis().isVertical() ? facing == Direction.DOWN ? 180 : 0 : 0)
                                .build();
                    })
            )
            .item(CanisterBlockItem::new)
            .model((c, p) -> p.blockItem(c, "/block")).build()
            .register();

    public static final BlockEntry<DistillationTankBlock> DISTILLATION_TANK = REGISTRATE.block("distillation_tank", DistillationTankBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .blockstate(new DistillationTankGenerator()::generate)
            .loot((lt, block) -> {
                LootTable.Builder builder = LootTable.lootTable();
                LootItemCondition.Builder survivesExplosion = ExplosionCondition.survivesExplosion();
                lt.add(block, builder.withPool(LootPool.lootPool()
                                .when(survivesExplosion)
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(CDGItems.DISTILLATION_CONTROLLER)))
                        .withPool(LootPool.lootPool().when(survivesExplosion)
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(AllBlocks.FLUID_TANK))));
            })
            .onRegister(CreateRegistrate.blockModel(() -> DistillationTankModel::new))
            .register();

    public static final BlockEntry<BulkFermenterBlock> BULK_FERMENTER = REGISTRATE.block("bulk_fermenter", BulkFermenterBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.METAL))
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .properties(p -> p.noOcclusion())
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.simpleBlock(c.getEntry(), AssetLookup.standardModel(c, p)))
            .onRegister(CreateRegistrate.connectedTextures(BulkFermenterCTBehavior::new))
            .item(MultiBlockContainerBlockItem::new)
            .build()
            .register();

    public static final BlockEntry<OilBarrelBlock> OIL_BARREL = REGISTRATE.block("oil_barrel", OilBarrelBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.METAL))
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .tag(AllTags.AllBlockTags.COPYCAT_ALLOW.tag)
            .blockstate((c, p) -> BlockStateGen.simpleBlock(c, p, bs -> p.models().getExistingFile(p.modLoc("block/oil_barrel" + (bs.getValue(OilBarrelBlock.AXIS).isVertical() ? "" : bs.getValue(OilBarrelBlock.AXIS) == Direction.Axis.Z ? "_sideways_clockwise" : "_sideways")))))
            .transform(mountedFluidStorage(CDGMountedStorageTypes.OIL_BARREL))
            .onRegister(CreateRegistrate.connectedTextures(OilBarrelCTBehavior::new))
            .item(MultiBlockContainerBlockItem::new)
            .build()
            .register();

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BLOCK = REGISTRATE.block("chip_wood_block", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .tag(BlockTags.PLANKS)
            .transform(axeOnly())
            .blockstate((c, p) -> p.axisBlock(c.getEntry(), p.modLoc("block/chip_wood_block_side"), p.modLoc("block/chip_wood_block")))
            .item().tag(ItemTags.PLANKS).build()
            .register();

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BEAM = REGISTRATE.block("chip_wood_beam", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.STRIPPED_OAK_LOG)
            .transform(axeOnly())
            .blockstate((c, p) -> p.logBlock(c.getEntry()))
            .simpleItem()
            .register();

    public static final BlockEntry<SlabBlock> CHIP_WOOD_SLAB = REGISTRATE.block("chip_wood_slab", SlabBlock::new)
            .initialProperties(() -> Blocks.OAK_SLAB)
            .transform(axeOnly())
            .blockstate((c, p) -> p.slabBlock(c.getEntry(), p.modLoc("block/chip_wood_block"), p.modLoc("block/chip_wood_block_side"), p.modLoc("block/chip_wood_block"), p.modLoc("block/chip_wood_block")))
            .item().tag(ItemTags.WOODEN_SLABS).build()
            .register();

    public static final BlockEntry<StairBlock> CHIP_WOOD_STAIRS = REGISTRATE.block("chip_wood_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS.defaultBlockState(), p))
            .initialProperties(() -> Blocks.OAK_STAIRS)
            .transform(axeOnly())
            .blockstate((c, p) -> p.stairsBlock(c.getEntry(), p.modLoc("block/chip_wood_block_side"), p.modLoc("block/chip_wood_block"), p.modLoc("block/chip_wood_block")))
            .item().tag(ItemTags.WOODEN_STAIRS).build()
            .register();

    public static final BlockEntry<Block> ASPHALT_BLOCK = REGISTRATE.block("asphalt_block", Block::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.speedFactor(1.25f))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.simpleBlock(c.get(), p.models()
                    .cubeAll(c.getName(), p.modLoc("block/asphalt"))))
            .simpleItem()
            .register();

    public static final BlockEntry<SlabBlock> ASPHALT_SLAB = REGISTRATE.block("asphalt_slab", SlabBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.speedFactor(1.25f))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.slabBlock(c.getEntry(), p.modLoc("block/asphalt_block"), p.modLoc("block/asphalt")))
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> ASPHALT_STAIRS = REGISTRATE.block("asphalt_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS.defaultBlockState(), p))
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.speedFactor(1.25f))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.stairsBlock(c.getEntry(), p.modLoc("block/asphalt")))
            .simpleItem()
            .register();

    public static final BlockEntry<AndesiteGirderBlock> ANDESITE_GIRDER =
            REGISTRATE.block("andesite_girder", AndesiteGirderBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
                    .transform(pickaxeOnly())
                    .blockstate(AndesiteGirderGenerator::blockState)
                    .item().model((c, p) -> p.blockItem(c, "/item")).build()
                    .register();

    public static final BlockEntry<AndesiteGirderEncasedShaftBlock> ANDESITE_GIRDER_ENCASED_SHAFT =
            REGISTRATE.block("andesite_girder_encased_shaft", AndesiteGirderEncasedShaftBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_GRAY).sound(SoundType.NETHERITE_BLOCK))
                    .transform(pickaxeOnly())
                    .blockstate(AndesiteGirderGenerator::blockStateWithShaft)
                    .loot((p, b) -> p.add(b, p.createSingleItemTable(ANDESITE_GIRDER.get())
                            .withPool(p.applyExplosionCondition(AllBlocks.SHAFT.get(), LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1.0F))
                                    .add(LootItem.lootTableItem(AllBlocks.SHAFT.get()))))))
                    .register();

    public static final BlockEntry<SheetMetalPanelBlock> SHEET_METAL_PANEL =
            REGISTRATE.block("sheet_metal_panel", SheetMetalPanelBlock::new)
                    .initialProperties(SharedProperties::softMetal)
                    .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).sound(SoundType.NETHERITE_BLOCK))
                    .onRegister(CreateRegistrate.blockModel(() -> SheetMetalPanelModel::new))
                    .transform(pickaxeOnly())
                    .blockstate((c, p) ->
                            p.getVariantBuilder(c.getEntry())
                                    .forAllStates(bs ->
                                    ConfiguredModel.builder()
                                            .modelFile(bs.getValue(SheetMetalPanelBlock.FACING).getAxis().isHorizontal() ? ((bs.getValue(SheetMetalPanelBlock.ROLL) ? p.models().getExistingFile(p.modLoc("block/sheet_metal_panel_horizontal")) : AssetLookup.standardModel(c, p))) : AssetLookup.standardModel(c, p))
                                            .rotationX(bs.getValue(SheetMetalPanelBlock.FACING).getAxis().isVertical() ? (bs.getValue(SheetMetalPanelBlock.FACING) == Direction.UP ? 90 : 270) : 0)
                                            .rotationY(bs.getValue(SheetMetalPanelBlock.FACING).getAxis().isVertical() ? (bs.getValue(SheetMetalPanelBlock.ROLL) ? 90 : 0) :
                                                    (bs.getValue(SheetMetalPanelBlock.FACING) == Direction.SOUTH ? 0 :
                                                            bs.getValue(SheetMetalPanelBlock.FACING) == Direction.NORTH ? 180 :
                                                                    bs.getValue(SheetMetalPanelBlock.FACING) == Direction.WEST  ? 90  : 270))
                                            .build()
                            ))
                    .simpleItem()
                    .register();

    public static final Map<DyeColor, BlockEntry<ConcreteEncasedFluidPipeBlock>> CONCRETE_ENCASED_FLUID_PIPES = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            CONCRETE_ENCASED_FLUID_PIPES.put(color,
                REGISTRATE.block(color.getName() + "_concrete_encased_fluid_pipe", ConcreteEncasedFluidPipeBlock::new)
                        .properties(p -> p.mapColor(color.getMapColor()).sound(SoundType.STONE))
                        .transform(pickaxeOnly())
                        .blockstate((c, p) -> {
                            MultiPartBlockStateBuilder builder = p.getMultipartBuilder(c.get());
                            builder.part()
                                    .modelFile(p.models().getExistingFile(ResourceLocation.withDefaultNamespace("block/" + color.getName() + "_concrete")))
                                    .addModel()
                                    .end();

                            builder.part()
                                    .modelFile(p.models().getExistingFile(p.modLoc("block/concrete/concrete_encased_pipe_part")))
                                    .addModel()
                                    .condition(BlockStateProperties.NORTH, true)
                                    .end();

                            builder.part()
                                    .modelFile(p.models().getExistingFile(p.modLoc("block/concrete/concrete_encased_pipe_part")))
                                    .rotationX(90)
                                    .addModel()
                                    .condition(BlockStateProperties.DOWN, true)
                                    .end();

                            builder.part()
                                    .modelFile(p.models().getExistingFile(p.modLoc("block/concrete/concrete_encased_pipe_part")))
                                    .rotationX(270)
                                    .addModel()
                                    .condition(BlockStateProperties.UP, true)
                                    .end();

                            builder.part()
                                    .modelFile(p.models().getExistingFile(p.modLoc("block/concrete/concrete_encased_pipe_part")))
                                    .rotationY(90)
                                    .addModel()
                                    .condition(BlockStateProperties.EAST, true)
                                    .end();

                            builder.part()
                                    .modelFile(p.models().getExistingFile(p.modLoc("block/concrete/concrete_encased_pipe_part")))
                                    .rotationY(270)
                                    .addModel()
                                    .condition(BlockStateProperties.WEST, true)
                                    .end();

                            builder.part()
                                    .modelFile(p.models().getExistingFile(p.modLoc("block/concrete/concrete_encased_pipe_part")))
                                    .rotationY(180)
                                    .addModel()
                                    .condition(BlockStateProperties.SOUTH, true)
                                    .end();
                        })
                        .loot((lt, block) -> lt.dropOther(block, AllBlocks.FLUID_PIPE))
                        .register()
            );
        }
    }
    public static void register() {
    }

    private static NonNullConsumer<? super Block> movementBehaviour(MovementBehaviour movementBehaviour) {
        return b -> MovementBehaviour.REGISTRY.register(b, movementBehaviour);
    }

}
