package shipwrights.genesis.teleportation.impl;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.teleportation.TeleportData;

import java.lang.reflect.Method;

public class ShipTeleporter {

    public static void teleportShip(
            long id,
            TeleportData data,
            ServerLevel newLevel,
            Object shipWorld
    ) {
        String dimId = newLevel.dimension().location().toString();
        Vector3dc pos = data.newPos();

        boolean teleported = false;
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            try {
                Method teleportMethod = sableClass.getMethod("teleport", long.class, ServerLevel.class, Vector3dc.class, Quaterniondc.class, Vector3dc.class, Vector3dc.class);
                teleported = (boolean) teleportMethod.invoke(null, id, newLevel, pos, data.rotation(), data.velocity(), data.omega());
            } catch (Exception e1) {
                try {
                    Method teleportMethod = sableClass.getMethod("teleportSubLevel", long.class, ServerLevel.class, Vector3dc.class);
                    teleported = (boolean) teleportMethod.invoke(null, id, newLevel, pos);
                } catch (Exception ignored) {
                }
            }
        } catch (Throwable ignored) {
        }

        if (!teleported) {
            String cmd = String.format("execute in %s run sable teleport %d %f %f %f",
                    dimId, id, pos.x(), pos.y(), pos.z());
            NeoGenesisMod.LOGGER.info("[ShipTeleporter] Executing fallback teleport command: {}", cmd);
            CommandSourceStack cmdSource = newLevel.getServer().createCommandSourceStack();
            newLevel.getServer().getCommands().performPrefixedCommand(cmdSource, cmd);
        }
    }

    public static void teleportSubLevel(
            Object subLevel,
            TeleportData data,
            ServerLevel newLevel
    ) {
        if (subLevel == null) return;
        long id = getSubLevelId(subLevel);
        teleportShip(id, data, newLevel, null);
    }

    private static long getSubLevelId(Object subLevel) {
        try {
            Method idMethod = subLevel.getClass().getMethod("getId");
            Object val = idMethod.invoke(subLevel);
            if (val instanceof Number num) {
                return num.longValue();
            }
        } catch (Exception ignored) {
        }
        return 0L;
    }
}