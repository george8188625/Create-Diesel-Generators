package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.jesz.createdieselgenerators.items.ItemRegistry;
import com.jesz.createdieselgenerators.other.FuelTypeManager;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.*;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Random;
import java.util.function.Supplier;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.SILENCED;
import static com.jesz.createdieselgenerators.blocks.LargeDieselGeneratorBlock.PIPE;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.UP;

public class DieselEngineScenes {
    static FluidStack currentFuel;
    public static void small(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("diesel_engine", "Setting up a Diesel Engine");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();

        Selection tank = util.select.fromTo(4, 0, 1, 4, 1, 1);
        BlockPos pumpPos = util.grid.at(3, 1, 1);
        BlockPos enginePos = util.grid.at(1, 2, 1);
        Selection engine = util.select.position(enginePos);
        Selection pump = util.select.position(pumpPos);
        Selection pipe = util.select.fromTo(1, 1, 1, 3, 1, 1);
        Selection cogs = util.select.fromTo(3, 0, 2, 4, 0, 2);

        scene.idle(15);
        ElementLink<WorldSectionElement> engineElement =
                scene.world.showIndependentSection(engine, Direction.DOWN);
        scene.world.moveSection(engineElement, util.vector.of(0, -1, 0), 0);
        scene.idle(15);
        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Diesel Generators are a compact way of generating kinetic energy.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(30);
        scene.world.hideIndependentSection(engineElement, Direction.UP);
        scene.idle(15);
        scene.world.moveSection(engineElement, util.vector.of(0, 1, 0), 0);
        scene.world.showIndependentSection(engine, Direction.DOWN);

        scene.world.showSection(pipe, Direction.WEST);
        scene.world.showSection(tank, Direction.NORTH);
        scene.idle(30);
        FuelTypeManager.tryPopulateTags();
        Supplier<FluidStack> content = () -> {
            currentFuel = new FluidStack(FuelTypeManager.fuelTypes.isEmpty() ? FluidRegistry.DIESEL.get() : FuelTypeManager.fuelTypes.keySet().stream().toList().get(new Random().nextInt(0, FuelTypeManager.fuelTypes.size() - 1)), 16000);
            return currentFuel;
        };
        scene.world.modifyBlockEntity(util.grid.at(4, 0, 1), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content.get(), IFluidHandler.FluidAction.EXECUTE));
        scene.world.showSection(cogs, Direction.NORTH);
        scene.idle(30);
        scene.overlay.showText(55)
                .attachKeyFrame()
                .text("Give it some fuel and it will produce kinetic energy.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 2, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(30);

        scene.idle(30);
        scene.world.modifyKineticSpeed(cogs, f -> 16f);
        scene.world.modifyKineticSpeed(pump, f -> -32f);
        scene.effects.rotationSpeedIndicator(util.grid.at(3, 0, 2));
        scene.world.modifyKineticSpeed(engine, f -> 96f);
        scene.effects.rotationSpeedIndicator(enginePos);
        scene.idle(20);
        scene.world.showSection(util.select.position(util.grid.at(1, 2, 0)), Direction.DOWN);
        scene.world.showSection(util.select.position(util.grid.at(1, 2, 2)), Direction.DOWN);
        scene.world.modifyKineticSpeed(util.select.fromTo(1, 2, 0, 1, 2, 2),f -> 96f);
        scene.idle(60);
    }
    public static void huge(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("huge_diesel_engine", "Setting up a Diesel Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection tank = util.select.fromTo(4, 1, 3, 4, 2, 3);
        Selection pipes = util.select.fromTo(0, 2, 0, 3, 2, 4);
        Selection engines = util.select.fromTo(0, 1, 0, 2, 1, 4);
        Selection shafts = util.select.fromTo(0, 1, 2, 2, 1, 2);
        Selection shafts2 = util.select.fromTo(3, 1, 2, 4, 1, 2);

        scene.world.showSection(engines, Direction.DOWN);
        scene.idle(10);
        scene.overlay.showText(20)
                .attachKeyFrame()
                .text("Huge Diesel Engines connect to Shafts ...")
                .pointAt(util.vector.blockSurface(util.grid.at(0, 1, 0), Direction.NORTH))
                .placeNearTarget();
        scene.idle(30);
        scene.world.showSection(shafts2, Direction.DOWN);
        scene.idle(15);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(2, 1, 4), Pointing.DOWN).withItem(new ItemStack(AllItems.WRENCH.get())), 15);
        scene.idle(20);
        scene.world.modifyBlock(util.grid.at(2, 1, 4), s -> s.setValue(UP, false), false);
        scene.idle(15);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(2, 1, 4), Pointing.DOWN).withItem(new ItemStack(AllItems.WRENCH.get())), 15);
        scene.idle(20);
        scene.world.modifyBlock(util.grid.at(2, 1, 4), s -> s.setValue(UP, true), false);
        scene.idle(15);
        scene.world.showSection(tank, Direction.DOWN);
        scene.idle(15);
        scene.world.showSection(pipes, Direction.DOWN);
        scene.idle(15);

        FuelTypeManager.tryPopulateTags();
        Supplier<FluidStack> content = () -> {
            currentFuel = new FluidStack(FuelTypeManager.fuelTypes.isEmpty() ? FluidRegistry.DIESEL.get() : FuelTypeManager.fuelTypes.keySet().stream().toList().get(new Random().nextInt(0, FuelTypeManager.fuelTypes.size() - 1)), 16000);
            return currentFuel;
        };
        scene.world.modifyBlockEntity(util.grid.at(4, 1, 3), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content.get(), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(15);
        scene.overlay.showText(40)
                .attachKeyFrame()
                .text("... they will start generating Kinetic Energy, once you give them some fuel.")
                .pointAt(util.vector.blockSurface(util.grid.at(0, 1, 0), Direction.NORTH))
                .placeNearTarget();
        scene.idle(50);
        scene.world.modifyKineticSpeed(shafts2, f -> 16f);
        scene.world.modifyKineticSpeed(shafts, f -> 16f);
        scene.world.modifyKineticSpeed(util.select.position(3, 2, 3), f -> -32f);
        scene.idle(30);
        scene.world.modifyKineticSpeed(shafts2, f -> 128f);
        scene.world.modifyKineticSpeed(shafts, f -> 128f);
        scene.world.modifyKineticSpeed(util.select.position(3, 2, 3), f -> -64f);
        scene.idle(10);
    }
    public static void silencer(SceneBuilder scene, SceneBuildingUtil util){
        scene.title("engine_silencer", "Applying an Engine Silencer");
        scene.configureBasePlate(0, 0, 3);
        scene.showBasePlate();

        BlockPos engine = util.grid.at(1, 1, 1);

        ElementLink<EntityElement> entity1 =
                scene.world.createItemEntity(new Vec3(1, 2, 1), util.vector.of(0, 0.2, 0), ItemRegistry.ENGINE_SILENCER.asStack());

        scene.overlay.showText(60)
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .text("Engine Silencers are used to make Diesel Engines Silent.")
                .pointAt(util.vector.centerOf(engine))
                .placeNearTarget();
        scene.idle(70);
        scene.world.modifyEntity(entity1, Entity::discard);
        scene.world.showSection(util.select.position(engine), Direction.DOWN);

        scene.world.modifyKineticSpeed(util.select.position(engine), f -> 96f);
        scene.idle(20);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 1, 1), Pointing.DOWN).withItem(ItemRegistry.ENGINE_SILENCER.asStack()),
                20);
        scene.world.modifyBlock(engine, s -> s.setValue(SILENCED, true), false);
        scene.idle(20);
        scene.overlay.showText(60)
                .attachKeyFrame()
                .colored(PonderPalette.GREEN)
                .text("Once you apply an Engine Silencer, the Diesel Engine will stop making any noises.")
                .pointAt(util.vector.centerOf(engine))
                .placeNearTarget();
        scene.idle(70);
    }
    public static void modular(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("large_diesel_engine", "Setting up a Modular Diesel Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        BlockPos pumpPos = util.grid.at(3, 1, 3);
        Selection mainEngine = util.select.position(1, 1, 1);
        Selection engines = util.select.fromTo(1, 1, 2, 1, 1, 3);

        Selection pump = util.select.position(pumpPos);
        Selection pipe = util.select.fromTo(1, 2, 1, 2 ,2, 1);
        Selection pipe2 = util.select.fromTo(2, 1, 1, 2 ,1, 3);
        Selection pipe3 = util.select.fromTo(3, 1, 3, 4, 2, 3);
        Selection cogs = util.select.fromTo(5, 1, 2, 3, 1, 2);
        Selection largeCog = util.select.position(5, 0, 1);

        scene.idle(15);
        scene.world.showSection(mainEngine, Direction.DOWN);

        scene.idle(15);
        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Modular Diesel Generators function like normal Diesel generators.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(60);

        scene.world.showSection(pipe, Direction.DOWN);
        scene.world.showSection(pipe2, Direction.DOWN);
        scene.world.showSection(pipe3, Direction.DOWN);

        scene.idle(15);

        scene.world.showSection(cogs, Direction.DOWN);
        scene.world.showSection(largeCog, Direction.DOWN);

        scene.idle(15);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Once you give them some fuel, they will produce Kinetic Energy ...")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();

        scene.idle(60);

        scene.world.modifyKineticSpeed(largeCog, s -> 16f);
        scene.world.modifyKineticSpeed(cogs, s -> -32f);
        scene.world.modifyKineticSpeed(pump, s -> 32f);
        scene.idle(10);

        FuelTypeManager.tryPopulateTags();
        Supplier<FluidStack> content = () -> {
            currentFuel = new FluidStack(FuelTypeManager.fuelTypes.isEmpty() ? FluidRegistry.DIESEL.get() : FuelTypeManager.fuelTypes.keySet().stream().toList().get(new Random().nextInt(0, FuelTypeManager.fuelTypes.size() - 1)), 1600);
            return currentFuel;
        };
        scene.world.modifyBlockEntity(util.grid.at(4, 1, 3), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .fill(content.get(), IFluidHandler.FluidAction.EXECUTE));

        scene.world.modifyKineticSpeed(mainEngine, s -> 96f);

        scene.effects.rotationSpeedIndicator(util.grid.at(1, 1, 1));

        scene.idle(15);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("... They can be stacked.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();

        scene.idle(60);

        scene.world.showSection(engines, Direction.EAST);

        scene.world.modifyBlocks(engines, s -> s.setValue(PIPE, true), false);

        scene.world.modifyKineticSpeed(engines, s -> 96f);
        scene.idle(20);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 1, 2), Pointing.DOWN).withItem(new ItemStack(AllItems.WRENCH.get())), 15);
        scene.world.modifyBlock(util.grid.at(1,1,2), s -> s.setValue(PIPE, false), false);
        scene.idle(30);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(1, 1, 3), Pointing.DOWN).withItem(new ItemStack(AllItems.WRENCH.get())), 15);
        scene.world.modifyBlock(util.grid.at(1,1,3), s -> s.setValue(PIPE, false), false);
        scene.idle(30);
        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("They will generate stress proportionally to how much engines you stack.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(60);
    }
}
