package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.CDGItems;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.createmod.ponder.foundation.element.InputWindowElement;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class DistillationScene {
    public static void scene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("distillation_tower", "Setting up a Distillation Tower");
        scene.configureBasePlate(1, 0, 5);
        scene.showBasePlate();

        Selection distillationTank = util.select().fromTo(3, 2, 2, 4, 4, 3);
        Selection windowedTank = util.select().fromTo(3, 2, 4, 4, 4, 5);
        Selection tank = util.select().fromTo(3, 2, 0, 4, 4, 1);
        Selection blazeBurners = util.select().fromTo(3, 1, 2, 4, 1, 3);


        Selection pipe = util.select().fromTo(1, 1, 2, 2, 2, 2);
        Selection pipe1 = util.select().fromTo(0, 0, 2, 0, 2, 2);

        Selection cog1 = util.select().position(0, 0, 4);
        Selection cog2 = util.select().position(1, 1, 3);
        Selection cog3 = util.select().position(0, 1, 3);
        Selection pump = util.select().position(1, 2, 2);

        scene.idle(15);
        ElementLink<WorldSectionElement> tankElement =
                scene.world().showIndependentSection(tank, Direction.DOWN);
        scene.world().moveSection(tankElement, util.vector().of(0, 0, 2), 0);
        scene.world().showSection(blazeBurners, Direction.NORTH);
        scene.idle(20);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Apply a Distillation Controller to a at least 3 block tall Fluid tank to create a Distillation Tower.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().topOf(2, 2, 2))
                .placeNearTarget();
        scene.idle(80);

        scene.overlay().showControls(util.vector().topOf(3, 3, 2), Pointing.LEFT, 20).withItem(CDGItems.DISTILLATION_CONTROLLER.asStack(12));
        scene.idle(15);
        ElementLink<WorldSectionElement> distillationTankElement =
                scene.world().showIndependentSectionImmediately(distillationTank);
        scene.world().moveSection(tankElement, util.vector().of(0, 10000, 2), 0);
        scene.world().hideIndependentSection(tankElement, Direction.DOWN);

        scene.idle(30);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Distillation Towers process fluids by distilling them")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().topOf(0, 2, 2))
                .placeNearTarget();
        scene.idle(80);

        scene.world().showSection(pipe, Direction.DOWN);
        scene.world().showSection(pipe1, Direction.DOWN);
        scene.world().showSection(cog3, Direction.DOWN);
        scene.world().showSection(cog2, Direction.DOWN);
        scene.world().showSection(cog1, Direction.DOWN);

        scene.idle(30);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Pump in your ingredient at the bottom level")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().topOf(0, 2, 2))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyKineticSpeed(cog1, f -> 16f);
        scene.world().modifyKineticSpeed(cog2, f -> -32f);
        scene.world().modifyKineticSpeed(cog3, f -> -32f);
        scene.world().modifyKineticSpeed(pump, f -> 64f);

        scene.idle(30);
        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Once you give the distiller the required heat level...")
                .pointAt(util.vector().centerOf(3, 1, 2))
                .placeNearTarget();
        scene.idle(80);

        scene.world().modifyBlocks(util.select().fromTo(3, 1, 2, 4, 1, 3), b -> b.setValue(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.KINDLED), false);
        scene.idle(15);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("... The distillation tower will start processing")
                .pointAt(util.vector().centerOf(3, 4, 2))
                .placeNearTarget();
        scene.idle(100);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Distillation Towers can have windows.")
                .colored(PonderPalette.BLUE)
                .pointAt(util.vector().centerOf(3, 4, 2))
                .placeNearTarget();
        scene.idle(80);


        scene.overlay().showControls(util.vector().topOf(3, 2, 3), Pointing.LEFT, 15).withItem(new ItemStack(AllItems.WRENCH.get()));
        scene.overlay().showControls(util.vector().topOf(4, 3, 2), Pointing.RIGHT, 15).withItem(new ItemStack(AllItems.WRENCH.get()));

        scene.idle(15);
        scene.world().hideIndependentSection(distillationTankElement, Direction.DOWN);
        scene.world().moveSection(distillationTankElement, util.vector().of(0, 10000, 0), 0);
        ElementLink<WorldSectionElement> windowedTankElement =
                scene.world().showIndependentSectionImmediately(windowedTank);
        scene.world().moveSection(windowedTankElement, util.vector().of(0, 0, -2), 0);

        scene.idle(60);
    }
}
