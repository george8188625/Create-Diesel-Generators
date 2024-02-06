package com.jesz.createdieselgenerators.blocks;

import com.jesz.createdieselgenerators.blocks.ct.DistillationTankModel;
import com.jesz.createdieselgenerators.blocks.ct.ModularDieselEngineCTBehavior;
import com.jesz.createdieselgenerators.blocks.ct.OilBarrelCTBehavior;
import com.jesz.createdieselgenerators.contraption.DieselEngineMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackBearingBMovementBehaviour;
import com.jesz.createdieselgenerators.contraption.PumpjackHeadMovementBehaviour;
import com.jesz.createdieselgenerators.items.CanisterBlockItem;
import com.jesz.createdieselgenerators.items.MultiBlockContainerBlockItem;
import com.jesz.createdieselgenerators.other.EngineStateDisplaySource;
import com.jesz.createdieselgenerators.other.OilAmountDisplaySource;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;
import static com.simibubi.create.AllMovementBehaviours.movementBehaviour;
import static com.simibubi.create.content.redstone.displayLink.AllDisplayBehaviours.assignDataBehaviour;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BlockRegistry {
public static final BlockEntry<DieselGeneratorBlock> DIESEL_ENGINE = REGISTRATE.block("diesel_engine", DieselGeneratorBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new EngineStateDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(movementBehaviour(new DieselEngineMovementBehaviour()))
            .simpleItem()
            .register();
    public static final BlockEntry<LargeDieselGeneratorBlock> MODULAR_DIESEL_ENGINE = REGISTRATE.block("large_diesel_engine", LargeDieselGeneratorBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new EngineStateDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(connectedTextures(ModularDieselEngineCTBehavior::new))
            .onRegister(movementBehaviour(new DieselEngineMovementBehaviour()))
            .simpleItem()
            .register();
    public static final BlockEntry<HugeDieselEngineBlock> HUGE_DIESEL_ENGINE = REGISTRATE.block("huge_diesel_engine", HugeDieselEngineBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_YELLOW))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new EngineStateDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();
    public static final BlockEntry<PoweredEngineShaftBlock> POWERED_ENGINE_SHAFT = REGISTRATE.block("powered_engine_shaft", PoweredEngineShaftBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(pickaxeOnly())
            .register();
    public static final BlockEntry<BasinLidBlock> BASIN_LID = REGISTRATE.block("basin_lid", BasinLidBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_GRAY))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();

    public static final BlockEntry<PumpjackBearingBlock> PUMPJACK_BEARING = REGISTRATE.block("pumpjack_bearing", PumpjackBearingBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();
    public static final BlockEntry<PumpjackHeadBlock> PUMPJACK_HEAD = REGISTRATE.block("pumpjack_head", PumpjackHeadBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(movementBehaviour(new PumpjackHeadMovementBehaviour()))
            .simpleItem()
            .register();
    public static final BlockEntry<PumpjackBearingBBlock> PUMPJACK_BEARING_B = REGISTRATE.block("pumpjack_bearing_b", PumpjackBearingBBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .onRegister(movementBehaviour(new PumpjackBearingBMovementBehaviour()))
            .register();
    public static final BlockEntry<PumpjackHoleBlock> PUMPJACK_HOLE = REGISTRATE.block("pumpjack_hole", PumpjackHoleBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_ORANGE))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .onRegister(assignDataBehaviour(new OilAmountDisplaySource()))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();
    public static final BlockEntry<PumpjackCrankBlock> PUMPJACK_CRANK = REGISTRATE.block("pumpjack_crank", PumpjackCrankBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .simpleItem()
            .register();

    public static final BlockEntry<CanisterBlock> CANISTER = REGISTRATE.block("canister", CanisterBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_CYAN))
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .item(CanisterBlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntry<DistillationTankBlock> DISTILLATION_TANK = REGISTRATE.block("distillation_tank", DistillationTankBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .onRegister(CreateRegistrate.blockModel(() -> DistillationTankModel::new))
            .register();

    public static final BlockEntry<OilBarrelBlock> OIL_BARREL = REGISTRATE.block("oil_barrel", OilBarrelBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .properties(BlockBehaviour.Properties::noOcclusion)
            .properties(p -> p.isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .onRegister(CreateRegistrate.connectedTextures(OilBarrelCTBehavior::new))
            .item(MultiBlockContainerBlockItem::new)
            .build()
            .register();

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BLOCK = REGISTRATE.block("chip_wood_block", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<RotatedPillarBlock> CHIP_WOOD_BEAM = REGISTRATE.block("chip_wood_beam", RotatedPillarBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<SlabBlock> CHIP_WOOD_SLAB = REGISTRATE.block("chip_wood_slab", SlabBlock::new)
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> CHIP_WOOD_STAIRS = REGISTRATE.block("chip_wood_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS::defaultBlockState, p))
            .initialProperties(() -> Blocks.OAK_PLANKS)
            .properties(p -> p)
            .simpleItem()
            .register();

    public static final BlockEntry<Block> ASPHALT_BLOCK = REGISTRATE.block("asphalt_block", Block::new)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.sound(SoundType.STONE))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .properties(p -> p.speedFactor(1.25f))
            .simpleItem()
            .register();
    public static final BlockEntry<SlabBlock> ASPHALT_SLAB = REGISTRATE.block("asphalt_slab", SlabBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.sound(SoundType.STONE))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .properties(p -> p.speedFactor(1.25f))
            .simpleItem()
            .register();

    public static final BlockEntry<StairBlock> ASPHALT_STAIRS = REGISTRATE.block("asphalt_stairs", p -> new StairBlock(Blocks.ANDESITE_STAIRS::defaultBlockState, p))
            .properties(p -> p.mapColor(MapColor.COLOR_BLACK))
            .properties(p -> p.sound(SoundType.STONE))
            .properties(p -> p.noOcclusion())
            .properties(p -> p.strength(3f))
            .properties(p -> p.speedFactor(1.25f))
            .simpleItem()
            .register();

    public static void register() {
    }
}
