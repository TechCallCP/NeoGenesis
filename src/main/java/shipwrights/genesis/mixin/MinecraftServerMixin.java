package shipwrights.genesis.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;
import org.sable.api.SableUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import shipwrights.genesis.NeoGenesisMod;

import java.util.Map;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Final
    @Shadow
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject(method = "prepareLevels", at = @At("RETURN"), remap = false)
    public void onLevelsCreated(ChunkProgressListener chunkProgressListener, CallbackInfo ci) {
        for (ServerLevel level : levels.values()) {
            if (NeoGenesisMod.isMiniScale(level)) {
                SableUtils.getShipObjectWorld(level).updateDimension(
                        SableUtils.getDimensionId(level), new Vector3d(), 63d, -1d
                );
            }
        }
    }
}