package com.jesz.createdieselgenerators.ponder;

import com.jesz.createdieselgenerators.content.burner.BurnerBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.element.ElementLink;
import net.createmod.ponder.api.element.WorldSectionElement;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

public class BurnerScenes {

    public static void scene(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("burner", "Setting up a Burner");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();

        Selection cogs = util.select().fromTo(1,1,3,1,1,5);
        Selection cogs2 = util.select().position(0, 0, 5);
        Selection pump = util.select().position(2, 1, 3);
        Selection pipes = util.select().fromTo(2, 1, 2, 2, 2, 4);
        Selection basin = util.select().position(2, 2, 1);
        Selection burner = util.select().position(2, 1, 1);
        Selection valve = util.select().position(1,1,1);
        Selection valveShaft = util.select().fromTo(1,1,1,2,1,1);

        scene.idle(10);
        scene.world().showSection(burner, Direction.DOWN);

        scene.idle(10);
        scene.world().showSection(basin, Direction.DOWN);
        scene.idle(10);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Burners can provide Heat to items processed in a Basin")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.world().hideSection(basin, Direction.UP);
        scene.idle(20);

        scene.world().showSection(pipes, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(cogs, Direction.DOWN);
        scene.world().showSection(cogs2, Direction.DOWN);
        scene.idle(10);
        scene.world().modifyKineticSpeed(pump, f -> 32f);
        scene.world().modifyKineticSpeed(cogs, f -> -32f);
        scene.world().modifyKineticSpeed(cogs2, f -> 16f);
        scene.idle(10);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("The burner needs to be fed a flammable liquid")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        ElementLink<WorldSectionElement> valveSection =
                scene.world().showIndependentSection(valve, Direction.SOUTH);
        scene.idle(10);
        scene.world().rotateSection(valveSection, 180, 0, 0, 20);
        scene.world().modifyKineticSpeed(valveShaft, f -> 16f);
        scene.idle(20);
        scene.world().modifyKineticSpeed(valveShaft, f -> 0f);
        scene.idle(10);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("The burners' burn rates can be controlled with a valve")
                .pointAt(util.vector().blockSurface(util.grid().at(1, 1, 1), Direction.NORTH))
                .placeNearTarget();
        scene.idle(80);

        scene.world().rotateSection(valveSection, 360, 0, 0, 20);
        scene.world().modifyKineticSpeed(valveShaft, f -> 256f);
        scene.idle(20);
        scene.world().modifyKineticSpeed(valveShaft, f -> 0f);
        scene.idle(60);

        scene.overlay().showText(70)
                .attachKeyFrame()
                .text("Additionally, you can power the burner with redstone to make it burn even quicker")
                .pointAt(util.vector().blockSurface(util.grid().at(2, 1, 1), Direction.NORTH))
                .placeNearTarget();

        scene.idle(80);
        scene.world().setBlock(util.grid().at(2, 1, 0), Blocks.REDSTONE_TORCH.defaultBlockState(), false);
        scene.world().showSection(util.select().position(2, 1, 0), Direction.DOWN);
        scene.world().modifyBlockEntity(util.grid().at(2, 1, 1), BurnerBlockEntity.class, be -> be.redstonePower = true);
        scene.idle(80);

    }
}
