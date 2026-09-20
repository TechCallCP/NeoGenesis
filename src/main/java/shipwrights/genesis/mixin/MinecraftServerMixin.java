package shipwrights.genesis.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import shipwrights.genesis.NeoGenesisMod;

import java.lang.reflect.Method;
import java.util.Map;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Final
    @Shadow
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject(method = "prepareLevels", at = @At("RETURN"))
    public void onLevelsCreated(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
        for (ServerLevel level : levels.values()) {
            if (NeoGenesisMod.isMiniScale(level)) {
                try {
                    Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
                    try {
                        Method updateDimMethod = sableClass.getMethod("updateDimension", Level.class, Vector3d.class, double.class, double.class);
                        updateDimMethod.invoke(null, level, new Vector3d(0, 0, 0), 63.0, -1.0);
                    } catch (Exception e1) {
                        try {
                            Method getWorldMethod = sableClass.getMethod("getShipObjectWorld", Level.class);
                            Object shipWorld = getWorldMethod.invoke(null, level);
                            if (shipWorld != null) {
                                Method updateDim = shipWorld.getClass().getMethod("updateDimension", String.class, Vector3d.class, double.class, double.class);
                                updateDim.invoke(shipWorld, level.dimension().location().toString(), new Vector3d(0, 0, 0), 63.0, -1.0);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }
}