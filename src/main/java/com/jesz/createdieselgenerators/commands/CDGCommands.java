package com.jesz.createdieselgenerators.commands;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.Random;

public class CDGCommands {
    public CDGCommands (CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cdg").then(Commands.literal("oil")
                .then(Commands.literal("get").executes((command) -> getOilChunk(command.getSource())))
                .then(Commands.literal("regenerate").executes((command) -> refreshOilChunk(command.getSource())))
                .then(Commands.literal("set").then(Commands.argument("amount", IntegerArgumentType.integer(0, 100000)).executes((command) -> setOilChunk(command.getSource(), command))))

                ));
    }

    private int getOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos((int) source.getPosition().x/16, (int) source.getPosition().z/16);

        int amount = CreateDieselGenerators.getOilAmount(source.getLevel().getBiome(new BlockPos(chunkPos.x*16, 64,  chunkPos.z*16)), chunkPos.x, chunkPos.z, source.getLevel().getSeed());

        OilChunksSavedData sd = OilChunksSavedData.load(source.getLevel());
        if(sd.getChunkOilAmount(chunkPos) != -1)
            amount = sd.getChunkOilAmount(chunkPos);


        source.sendSuccess(Component.literal("There is ").withStyle(ChatFormatting.GRAY).append(Component.literal(amount+"B").withStyle(ChatFormatting.GOLD)).append(" of Oil in this Chunk.").withStyle(ChatFormatting.GRAY), false);

        return 1;
    }
    private int refreshOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos((int) source.getPosition().x/16, (int) source.getPosition().z/16);

        OilChunksSavedData sd = OilChunksSavedData.load(source.getLevel());
        sd.removeChunkAmount(chunkPos);

        source.sendSuccess(Component.literal("Refreshed this chunks oil contents").withStyle(ChatFormatting.GRAY), false);

        return 1;
    }
    private int setOilChunk(CommandSourceStack source, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos((int) source.getPosition().x/16, (int) source.getPosition().z/16);

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        OilChunksSavedData sd = OilChunksSavedData.load(source.getLevel());
            sd.setChunkAmount(chunkPos, amount);
        source.sendSuccess(Component.literal("Set this chunk's oil deposits to  ").withStyle(ChatFormatting.GRAY).append(Component.literal(amount+"B").withStyle(ChatFormatting.GOLD)), false);

        return 1;
    }
}
