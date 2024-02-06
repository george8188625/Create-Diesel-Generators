package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.blocks.BlockRegistry;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackCrankBlockEntity;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.*;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OilScenes {
    public static void pumpjack(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("pumpjack", "Setting up a Pumpjack");
        scene.configureBasePlate(0, 0, 9);
        scene.setSceneOffsetY(-2);
        scene.showBasePlate();
        scene.scaleSceneView(0.75f);
        scene.world.showSection(util.select.fromTo(0, 1, 0, 8, 1, 8), Direction.UP);

        Selection pipes = util.select.fromTo(4, 2, 0, 8, 3, 3);
        Selection pumpjackStand0 = util.select.fromTo(3, 2, 4, 3, 5, 4);
        Selection pumpjackStand1 = util.select.fromTo(5, 2, 4, 5, 5, 4);
        Selection pumpjack = util.select.fromTo(4, 4, 0, 4, 6, 8);

        scene.world.showSection(pumpjackStand1, Direction.DOWN);
        scene.world.showSection(pumpjackStand0, Direction.DOWN);
        scene.idle(15);
        scene.world.showSection(pumpjack, Direction.DOWN);
        scene.idle(30);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(4, 5, 8), Pointing.LEFT).withItem(AllItems.WRENCH.asStack()), 15);
        scene.idle(15);
        scene.world.setBlock(new BlockPos(4, 5, 8), BlockRegistry.PUMPJACK_BEARING_B.getDefaultState(), false);
        scene.idle(30);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(4, 5, 8), Pointing.LEFT).withItem(AllItems.SUPER_GLUE.asStack()), 15);
        scene.idle(25);
        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(4, 4, 1), Pointing.LEFT).withItem(AllItems.SUPER_GLUE.asStack()), 15);
        scene.idle(25);
        scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, util.select.fromTo(4, 5, 9, 4, 4, 0), new AABB(4, 6, 9, 5, 4, 0), 30);
        scene.idle(15);
        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(4, 4, 1), Pointing.LEFT).withItem(AllItems.SUPER_GLUE.asStack()), 15);
        scene.idle(25);
        scene.overlay.showControls(new InputWindowElement(util.vector.centerOf(5, 4, 3), Pointing.LEFT).withItem(AllItems.SUPER_GLUE.asStack()), 15);
        scene.idle(25);
        scene.overlay.chaseBoundingBoxOutline(PonderPalette.GREEN, util.select.fromTo(4, 3, 0, 4, 5, 1), new AABB(4, 4, 0, 5, 7, 2), 30);
        scene.idle(15);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Pumpjacks allow you to acquire Crude Oil ...")
                .pointAt(util.vector.topOf(4, 5, 0))
                .placeNearTarget();

        scene.idle(60);
        scene.world.showSection(util.select.position(4, 2, 8), Direction.WEST);
        scene.world.showSection(util.select.fromTo(4, 2, 9, 3, 1, 9), Direction.WEST);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("... to start using pumpjacks, attach a Pumpjack Crank ...")
                .pointAt(util.vector.topOf(4, 2, 8))
                .placeNearTarget();

        scene.idle(60);

        scene.overlay.showText(80)
                .attachKeyFrame()
                .text("... and add a pipe to bedrock.")
                .pointAt(util.vector.topOf(4, 1, 0))
                .placeNearTarget();

        scene.world.setBlock(new BlockPos(4, 1, 0), Blocks.AIR.defaultBlockState(), false);

        ElementLink<WorldSectionElement> pipesLink = scene.world.showIndependentSection(pipes, Direction.DOWN);
        scene.world.moveSection(pipesLink, new Vec3(0, -1, 0), 0);
        scene.idle(20);
        scene.world.setBlock(new BlockPos(4, 3, 0), Blocks.AIR.defaultBlockState(), false);

        scene.idle(15);
        scene.world.setBlock(new BlockPos(4, 3, 0), BlockRegistry.PUMPJACK_HOLE.getDefaultState(), false);

        scene.idle(15);
        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Pumpjacks can only pump out oil from Oil Chunks.")
                .pointAt(util.vector.topOf(6, 4, 5))
                .placeNearTarget();
        scene.idle(60);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Once assembled, it will start producing oil.")
                .pointAt(util.vector.topOf(6, 4, 5))
                .placeNearTarget();
        scene.idle(60);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(6, 4, 5), Pointing.LEFT).rightClick(), 15);
        scene.idle(50);
        scene.world.modifyBlockEntity(new BlockPos(4, 2, 8), PumpjackCrankBlockEntity.class, be -> be.crankSize.setValue(1));
        scene.world.moveSection(scene.world.makeSectionIndependent(util.select.fromTo(3, 4, 0, 5, 6, 8)), new Vec3(0, 1, 0), 10);
        scene.world.showSection(util.select.fromTo(3, 4, 4, 5, 4, 4), Direction.EAST);
        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("Pumpjack Cranks can be large, or small")
                .pointAt(util.vector.topOf(4, 2, 8))
                .placeNearTarget();

        scene.idle(60);
    }
}
