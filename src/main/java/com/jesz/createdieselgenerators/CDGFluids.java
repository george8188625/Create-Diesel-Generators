package com.jesz.createdieselgenerators;

import com.jesz.createdieselgenerators.content.concrete.ConcreteBucketItem;
import com.jesz.createdieselgenerators.content.concrete.ConcreteFluid;
import com.simibubi.create.AllFluids;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.jesz.createdieselgenerators.CreateDieselGenerators.REGISTRATE;

public class CDGFluids {

    public static final FluidEntry<ForgeFlowingFluid.Flowing> PLANT_OIL =
            REGISTRATE.fluid("plant_oil", CreateDieselGenerators.rl("block/plant_oil_still"), CreateDieselGenerators.rl("block/plant_oil_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new)
                    .block()
                    .build()
                    .bucket()
                    .onRegister(CDGFluids::registerFluidDispenseBehavior)
                    .build()
                    .register();
    
    public static final FluidEntry<ForgeFlowingFluid.Flowing> CRUDE_OIL =
            REGISTRATE.fluid("crude_oil", CreateDieselGenerators.rl("block/crude_oil_still"), CreateDieselGenerators.rl("block/crude_oil_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(100))
                    .fluidProperties(p -> p.levelDecreasePerBlock(3)
                            .tickRate(25)
                            .slopeFindDistance(2)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new)
                    .block()
                    .build()
                    .bucket()
                    .onRegister(CDGFluids::registerFluidDispenseBehavior)
                    .build()
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> BIODIESEL =
            REGISTRATE.fluid("biodiesel", CreateDieselGenerators.rl("block/biodiesel_still"), CreateDieselGenerators.rl("block/biodiesel_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new)
                    .block()
                    .build()
                    .bucket()
                    .onRegister(CDGFluids::registerFluidDispenseBehavior)
                    .build()
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> DIESEL =
            REGISTRATE.fluid("diesel", CreateDieselGenerators.rl("block/diesel_still"), CreateDieselGenerators.rl("block/diesel_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new)
                    .block()
                    .build()
                    .bucket()
                    .onRegister(CDGFluids::registerFluidDispenseBehavior)
                    .build()
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> GASOLINE =
            REGISTRATE.fluid("gasoline", CreateDieselGenerators.rl("block/gasoline_still"), CreateDieselGenerators.rl("block/gasoline_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(3)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new)
                    .block()
                    .build()
                    .bucket()
                    .onRegister(CDGFluids::registerFluidDispenseBehavior)
                    .build()
                    .register();

    public static final FluidEntry<ForgeFlowingFluid.Flowing> ETHANOL =
            REGISTRATE.fluid("ethanol", CreateDieselGenerators.rl("block/ethanol_still"), CreateDieselGenerators.rl("block/ethanol_flow"))
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(2)
                            .tickRate(25)
                            .slopeFindDistance(5)
                            .explosionResistance(100f))
                    .source(ForgeFlowingFluid.Source::new)
                    .block()
                    .build()
                    .bucket()
                    .onRegister(CDGFluids::registerFluidDispenseBehavior)
                    .build()
                    .register();


    public static final Map<DyeColor, FluidEntry<ForgeFlowingFluid.Flowing>> CONCRETE = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()) {
            CONCRETE.put(color,
                    REGISTRATE.fluid(color.getName() + "_cement", CreateDieselGenerators.rl("block/cement/" + color.getName() + "_still"), CreateDieselGenerators.rl("block/cement/" + color.getName() + "_flow"))
                            .lang(RegistrateLangProvider.toEnglishName(color.getName().replace('_', ' ')) + " Concrete")
                    .properties(b -> b.viscosity(1500)
                            .density(500))
                    .fluidProperties(p -> p.levelDecreasePerBlock(8)
                            .tickRate(12)
                            .slopeFindDistance(1)
                            .explosionResistance(100f))
                    .source(p -> new ConcreteFluid(p, color))
                    .bucket((f, p) -> new ConcreteBucketItem(color, f, p))
                            .lang(RegistrateLangProvider.toEnglishName(color.getName().replace('_', ' ')) + " Concrete Bucket")
                            .onRegister(CDGFluids::registerFluidDispenseBehavior)
                            .build()
                    .register()
            );
        }
    }

    public static void register() {}

    // from Create

    private static final DispenseItemBehavior DEFAULT = new DefaultDispenseItemBehavior();

    private static final DispenseItemBehavior DISPENSE_FLUID = new DefaultDispenseItemBehavior(){
        @Override
        protected ItemStack execute(BlockSource pSource, ItemStack pStack) {
            DispensibleContainerItem dispensibleContainerItem = (DispensibleContainerItem) pStack.getItem();
            BlockPos pos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
            Level level = pSource.getLevel();
            if (dispensibleContainerItem.emptyContents(null, level, pos, null, pStack)) {
                return new ItemStack(Items.BUCKET);
            }
            return DEFAULT.dispense(pSource, pStack);
        }
    };

    private static void registerFluidDispenseBehavior(BucketItem bucket) {
        DispenserBlock.registerBehavior(bucket, DISPENSE_FLUID);
    }
}
