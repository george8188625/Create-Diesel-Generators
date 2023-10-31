package com.jesz.createdieselgenerators.commands;

import com.jesz.createdieselgenerators.CreateDieselGenerators;
import com.jesz.createdieselgenerators.world.OilChunksSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CDGCommands {
    public CDGCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cdg").then(Commands.literal("oil")
                .then(Commands.literal("get").executes((command) -> getOilChunk(command.getSource())))
                .then(Commands.literal("locate").executes((command) -> locateOilChunk(command.getSource())))
                .then(Commands.literal("regenerate").executes((command) -> refreshOilChunk(command.getSource())))
                .then(Commands.literal("set").then(Commands.argument("amount", IntegerArgumentType.integer(0, 100000)).executes((command) -> setOilChunk(command.getSource(), command))))

                ));
    }

    private int getOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) source.getPosition().x, (int) source.getPosition().y, (int) source.getPosition().z));
        int amount = CreateDieselGenerators.getOilAmount(source.getLevel().getBiome(new BlockPos(chunkPos.x*16, 64,  chunkPos.z*16)), chunkPos.x, chunkPos.z, source.getLevel().getSeed());

        OilChunksSavedData sd = OilChunksSavedData.load(source.getLevel());
        if(sd.getChunkOilAmount(chunkPos) != -1)
            amount = sd.getChunkOilAmount(chunkPos);


        int finalAmount = amount;
        source.sendSuccess(() -> Component.literal("There is ").withStyle(ChatFormatting.GRAY).append(Component.literal(finalAmount +"B").withStyle(ChatFormatting.GOLD)).append(" of Oil in this Chunk.").withStyle(ChatFormatting.GRAY), false);

        return 1;
    }
    private int refreshOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) source.getPosition().x, (int) source.getPosition().y, (int) source.getPosition().z));

        OilChunksSavedData sd = OilChunksSavedData.load(source.getLevel());
        sd.removeChunkAmount(chunkPos);

        source.sendSuccess(() -> Component.literal("Refreshed this chunks oil contents").withStyle(ChatFormatting.GRAY), false);

        return 1;
    }
    private int setOilChunk(CommandSourceStack source, CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        ChunkPos chunkPos = new ChunkPos(new BlockPos((int) source.getPosition().x, (int) source.getPosition().y, (int) source.getPosition().z));

        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        OilChunksSavedData sd = OilChunksSavedData.load(source.getLevel());
            sd.setChunkAmount(chunkPos, amount);
        source.sendSuccess(() -> Component.literal("Set this chunk's oil deposits to  ").withStyle(ChatFormatting.GRAY).append(Component.literal(amount+"B").withStyle(ChatFormatting.GOLD)), false);

        return 1;
    }

    private int locateOilChunk(CommandSourceStack source) throws CommandSyntaxException {
        if(!source.hasPermission(2))
            return 0;
        Map<ChunkPos, Integer> oilChunks = new HashMap<>();
        for (int x = -10; x < 10; x++) {
            for (int z = -10; z < 10; z++) {
//                ChunkPos chunkPos = new ChunkPos((int) (source.getPosition().x + x * 16)/16, (int) (source.getPosition().z + z * 16)/16);
                ChunkPos chunkPos = new ChunkPos(new BlockPos((int) source.getPosition().x, (int) source.getPosition().y, (int) source.getPosition().z));
                chunkPos = new ChunkPos(chunkPos.x + x, chunkPos.z + z);

                OilChunksSavedData sd = OilChunksSavedData.load(source.getLevel());
                int amount = sd.getChunkOilAmount(chunkPos);
                if(amount == -1)
                    amount = CreateDieselGenerators.getOilAmount(source.getLevel().getBiome(new BlockPos(chunkPos.x*16, 64,  chunkPos.z*16)), chunkPos.x, chunkPos.z, source.getLevel().getSeed());

                if(amount != 0){
                    oilChunks.put(chunkPos, amount);
                }
            }
        }
        AtomicInteger closestAmount = new AtomicInteger(0);
        AtomicInteger closestX = new AtomicInteger(0);
        AtomicInteger closestZ = new AtomicInteger(0);
        AtomicReference<Float> closestDst = new AtomicReference<>(10000f);
        oilChunks.forEach((k, v) -> {
            float dst = (float) Math.sqrt(
                    Math.abs(source.getPosition().x/16-k.x)*
                    Math.abs(source.getPosition().x/16-k.x)+
                    Math.abs(source.getPosition().z/16-k.z)*
                    Math.abs(source.getPosition().z/16-k.z)
            );
            if(dst < closestDst.get()){
                closestDst.set(dst);
                closestX.set(k.x);
                closestZ.set(k.z);
                closestAmount.set(v);
            }

        });
        if(closestAmount.get() != 0) {
            int finalX = closestX.get();
            int finalZ = closestZ.get();

            source.sendSuccess(() -> Component.literal("There is oil in the chunk ").withStyle(ChatFormatting.GRAY).append(Component.literal( finalX + " " + finalZ).withStyle(ChatFormatting.GOLD).withStyle(
                    a -> a.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + finalX*16 + " ~ " + finalZ*16)
                    ))).append(" with ").withStyle(ChatFormatting.GRAY).append(Component.literal(closestAmount + "B ").withStyle(ChatFormatting.GOLD)).append(Component.literal("of oil.").withStyle(ChatFormatting.GRAY)), false);
            return 1;
        }
        source.sendFailure(Component.literal("There is no oil chunk nearby").withStyle(ChatFormatting.RED));
        return 1;
    }
}
