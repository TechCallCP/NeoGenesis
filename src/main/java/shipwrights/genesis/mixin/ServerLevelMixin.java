package shipwrights.genesis.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import org.sable.api.SableUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.networking.GenesisNetworking;
import shipwrights.genesis.networking.SyncTimeOffsetPacket;
import shipwrights.genesis.time.GenesisTimeData;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow public abstract ServerLevel getLevel();

    @Inject(method = "setDayTime", at = @At("HEAD"), remap = false)
    private void genesis$onSetDayTime(long newDayTime, CallbackInfo ci) {
        ServerLevel self = getLevel();
        if (!self.dimension().equals(Level.OVERWORLD)) return;

        long oldDayTime = NeoGenesisMod.getTicks(self);
        long delta = newDayTime - oldDayTime;
        if (delta == 0) return;

        GenesisTimeData data = GenesisTimeData.getOrCreate(self.getServer());
        data.addOffset(delta);
        GenesisNetworking.sendToAll(GenesisNetworking.INSTANCE, new SyncTimeOffsetPacket(data.getTimeOffset()));
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDayTime()J"), remap = false)
    private static long useGenesisDayTime(ServerLevel instance, Operation<Long> original) {
        return NeoGenesisMod.getTicks(instance);
    }

    @Inject(method = "addEntity", at = @At("HEAD"), remap = false)
    private void addEntityMixin(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        NeoGenesisMod.refreshEntityScaling(entity, getLevel());
    }

    @Inject(method = "addPlayer", at = @At("HEAD"), remap = false)
    private void addPlayerMixin(ServerPlayer arg, CallbackInfo ci) {
        NeoGenesisMod.refreshEntityScaling(arg, getLevel());
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"), remap = false)
    private boolean genesis$shouldSnow(Biome instance, LevelReader levelReader, BlockPos pos, Operation<Boolean> original) {
        ServerLevel level = getLevel();
        if (NeoGenesisMod.shouldCancelVoidDamage(level)) {
            return false;
        }

        var ship = SableUtils.getShipManagingPos(level, pos);
        if (ship != null && ship.getTransform().getPositionInWorld().y() > 400) {
            return false;
        }

        return original.call(instance, level, pos);
    }
}