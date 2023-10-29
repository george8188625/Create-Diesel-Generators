package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.blocks.PumpjackCrankBlock;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackBearingBlockEntity;
import com.jesz.createdieselgenerators.blocks.entity.PumpjackCrankBlockEntity;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.ponder.ElementLink;
import com.simibubi.create.foundation.ponder.SceneBuilder;
import com.simibubi.create.foundation.ponder.SceneBuildingUtil;
import com.simibubi.create.foundation.ponder.Selection;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.utility.Pointing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class OilScenes {
    public static void pumpjack(SceneBuilder scene, SceneBuildingUtil util) {
        scene.title("pumpjack", "Setting up a Pumpjack");
        scene.configureBasePlate(0, 0, 9);
        scene.showBasePlate();
        scene.world.showSection(util.select.fromTo(0, 1, 0, 8, 1, 8), Direction.UP);

        Selection pipes = util.select.fromTo(4, 2, 0, 8, 3, 3);
        Selection pumpjackStand0 = util.select.fromTo(3, 2, 5, 3, 5, 5);
        Selection pumpjackStand1 = util.select.fromTo(5, 2, 5, 6, 5, 5);
        Selection pumpjack = util.select.fromTo(4, 4, 0, 4, 6, 8);

        scene.world.showSection(pumpjackStand1, Direction.DOWN);
        scene.world.showSection(pumpjackStand0, Direction.DOWN);
        scene.idle(15);
        scene.world.showSection(pumpjack, Direction.DOWN);

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

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("... and add a pipe to bedrock.")
                .pointAt(util.vector.topOf(4, 1, 0))
                .placeNearTarget();

        scene.world.setBlock(new BlockPos(4, 1, 0), Blocks.AIR.defaultBlockState(), false);

        ElementLink<WorldSectionElement> pipesLink = scene.world.showIndependentSection(pipes, Direction.DOWN);
        scene.world.moveSection(pipesLink, new Vec3(0, -1, 0), 0);

        scene.idle(20);
        scene.overlay.showControls(new InputWindowElement(util.vector.topOf(4, 2, 0), Pointing.DOWN).withItem(new ItemStack(AllBlocks.COPPER_CASING.get())), 15);

        scene.idle(35);

        scene.world.setBlock(new BlockPos(4, 3, 0), AllBlocks.ENCASED_FLUID_PIPE.getDefaultState()
                .setValue(BlockStateProperties.SOUTH, true), false);

        scene.idle(15);

        scene.overlay.showText(50)
                .attachKeyFrame()
                .text("When assembled, it will start producing oil.")
                .pointAt(util.vector.topOf(4, 2, 0))
                .placeNearTarget();
        scene.idle(60);
        scene.world.modifyBlockEntity(new BlockPos(4, 2, 8), PumpjackCrankBlockEntity.class, be -> be.crankSize.setValue(1));
        scene.idle(15);
        scene.world.modifyBlockEntity(new BlockPos(4, 2, 8), PumpjackCrankBlockEntity.class, be -> be.crankSize.setValue(0));
        scene.idle(15);
        scene.world.modifyBlockEntity(new BlockPos(4, 2, 8), PumpjackCrankBlockEntity.class, be -> be.crankSize.setValue(1));
        scene.idle(15);
        scene.world.modifyBlockEntity(new BlockPos(4, 2, 8), PumpjackCrankBlockEntity.class, be -> be.crankSize.setValue(0));

        scene.idle(60);
    }
}
