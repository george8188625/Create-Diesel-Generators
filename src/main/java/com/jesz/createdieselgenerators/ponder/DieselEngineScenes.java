package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.fluids.FluidRegistry;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import static com.jesz.createdieselgenerators.blocks.DieselGeneratorBlock.POWERED;

public class DieselEngineScenes {
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
        scene.world.modifyBlock(util.grid.at(1, 2, 1), s -> s.setValue(POWERED, false)  , false);
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
        scene.world.modifyBlock(enginePos, s -> s.setValue(POWERED, false)  , false);
        scene.world.showSection(pipe, Direction.WEST);
        scene.world.showSection(tank, Direction.NORTH);
        scene.idle(30);
        scene.world.showSection(cogs, Direction.NORTH);
        scene.idle(30);
        scene.overlay.showText(70)
                .attachKeyFrame()
                .text("Give it some fuel and it will produce kinetic energy.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 2, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(30);
        scene.world.modifyKineticSpeed(cogs, f -> 16f);
        scene.world.modifyKineticSpeed(pump, f -> -32f);
        scene.effects.rotationSpeedIndicator(util.grid.at(3, 0, 2));

        FluidStack content = new FluidStack(FluidRegistry.BIODIESEL.get()
                .getSource(), 300);
        scene.world.modifyBlockEntity(util.grid.at(4, 0, 1), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .drain(content, IFluidHandler.FluidAction.EXECUTE));
        scene.world.modifyKineticSpeed(engine, f -> 96f);
        scene.world.modifyBlock(enginePos, s -> s.setValue(POWERED, true)  , false);
        scene.effects.rotationSpeedIndicator(enginePos);
        scene.idle(20);
        scene.world.modifyKineticSpeed(cogs, f -> 0f);
        scene.world.modifyKineticSpeed(pump, f -> 0f);
        scene.idle(20);
        scene.world.showSection(util.select.position(util.grid.at(1, 2, 0)), Direction.DOWN);
        scene.world.showSection(util.select.position(util.grid.at(1, 2, 2)), Direction.DOWN);
        scene.world.modifyKineticSpeed(util.select.fromTo(1, 2, 0, 1, 2, 2),f -> 96f);
        scene.idle(60);
    }
    public static void modular(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("large_diesel_engine", "Setting up a Modular Diesel Engine");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection tank = util.select.fromTo(4, 1, 3, 4, 2, 3);
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
        scene.world.modifyBlock(util.grid.at(1, 1, 1), s -> s.setValue(POWERED, false), false);
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

        FluidStack content = new FluidStack(FluidRegistry.BIODIESEL.get()
                .getSource(), 300);
        scene.world.modifyBlockEntity(util.grid.at(4, 1, 3), FluidTankBlockEntity.class, be -> be.getTankInventory()
                .drain(content, IFluidHandler.FluidAction.EXECUTE));

        scene.world.modifyKineticSpeed(mainEngine, s -> 96f);
        scene.world.modifyBlock(util.grid.at(1, 1, 1), s -> s.setValue(POWERED, true), false);

        scene.effects.rotationSpeedIndicator(util.grid.at(1, 1, 1));

        scene.idle(15);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("... They can be stacked.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();

        scene.idle(60);

        scene.world.showSection(engines, Direction.EAST);

        scene.world.modifyKineticSpeed(engines, s -> 96f);
        scene.world.modifyBlocks(engines, s -> s.setValue(POWERED, true), false);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("They will generate stress proportionally to how much engines you stack.")
                .pointAt(util.vector.blockSurface(util.grid.at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();

        scene.idle(60);
    }
}
