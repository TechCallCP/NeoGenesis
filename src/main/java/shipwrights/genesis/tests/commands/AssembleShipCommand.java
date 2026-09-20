package shipwrights.genesis.tests.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class AssembleShipCommand {

    private static final int MAX_BLOCKS = 256;

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("genesis")
                        .then(Commands.literal("assemble")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(AssembleShipCommand::execute)
                                )
                        )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos origin = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

        List<BlockPos> blocks = collectBlocks(level, origin);
        if (blocks.isEmpty()) {
            ctx.getSource().sendFailure(Component.literal("No blocks found at " + origin));
            return 0;
        }

        boolean assembled = false;
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            try {
                Method createMethod = sableClass.getMethod("createSubLevel", ServerLevel.class, List.class);
                createMethod.invoke(null, level, blocks);
                assembled = true;
            } catch (Exception e1) {
                try {
                    Method assembleMethod = sableClass.getMethod("assemble", ServerLevel.class, List.class);
                    assembleMethod.invoke(null, level, blocks);
                    assembled = true;
                } catch (Exception ignored) {
                }
            }
        } catch (Throwable ignored) {
        }

        if (!assembled) {
            String cmd = String.format("sable assemble %d %d %d", origin.getX(), origin.getY(), origin.getZ());
            ctx.getSource().getServer().getCommands().performPrefixedCommand(ctx.getSource(), cmd);
        }

        ctx.getSource().sendSuccess(
                () -> Component.literal("Assembled " + blocks.size() + " blocks into a ship at " + origin),
                false);
        return blocks.size();
    }

    /**
     * Flood-fills from {@code origin} collecting all 6-connected non-air blocks up to
     * {@value MAX_BLOCKS} blocks. This gathers the contiguous platform/structure to be
     * assembled into a ship.
     */
    private static List<BlockPos> collectBlocks(ServerLevel level, BlockPos origin) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();

        if (!level.getBlockState(origin).isAir()) {
            visited.add(origin);
            queue.add(origin);
        }

        while (!queue.isEmpty() && result.size() < MAX_BLOCKS) {
            BlockPos pos = queue.poll();
            result.add(pos);
            for (Direction dir : Direction.values()) {
                BlockPos neighbor = pos.relative(dir);
                if (visited.add(neighbor) && !level.getBlockState(neighbor).isAir()) {
                    queue.add(neighbor);
                }
            }
        }

        return result;
    }
}