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
import net.minecraft.world.phys.AABB;

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

import java.lang.reflect.Method;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Shadow
    public abstract ServerLevel getLevel();

    @Inject(method = "setDayTime", at = @At("HEAD"))
    private void genesis$onSetDayTime(long newDayTime, CallbackInfo ci) {
        ServerLevel self = getLevel();
        if (!self.dimension().equals(Level.OVERWORLD)) return;

        long oldDayTime = NeoGenesisMod.getTicks(self);
        long delta = newDayTime - oldDayTime;
        if (delta == 0) return;

        GenesisTimeData data = GenesisTimeData.getOrCreate(self.getServer());
        data.addOffset(delta);
        GenesisNetworking.sendToAll(new SyncTimeOffsetPacket(data.getTimeOffset()));
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDayTime()J"))
    private static long useGenesisDayTime(ServerLevel instance, Operation<Long> original) {
        return NeoGenesisMod.getTicks(instance);
    }

    @Inject(method = "addEntity", at = @At("HEAD"))
    private void addEntityMixin(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        NeoGenesisMod.refreshEntityScaling(entity, getLevel());
    }

    @Inject(method = "addPlayer", at = @At("HEAD"))
    private void addPlayerMixin(ServerPlayer player, CallbackInfo ci) {
        NeoGenesisMod.refreshEntityScaling(player, getLevel());
    }

    @WrapOperation(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;shouldSnow(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean genesis$shouldSnow(Biome instance, LevelReader levelReader, BlockPos pos, Operation<Boolean> original) {
        ServerLevel level = getLevel();
        if (NeoGenesisMod.shouldCancelVoidDamage(level)) {
            return false;
        }

        if (isPosInHighShip(level, pos)) {
            return false;
        }

        return original.call(instance, levelReader, pos);
    }

    private static boolean isPosInHighShip(ServerLevel level, BlockPos pos) {
        try {
            Class<?> sableClass = Class.forName("dev.ryanhcode.sable.Sable");
            Object container = null;
            try {
                Method getContainer = sableClass.getMethod("getSubLevelContainer", Level.class);
                container = getContainer.invoke(null, level);
            } catch (Exception ignored) {
            }

            if (container != null) {
                Method getAll = container.getClass().getMethod("getAllSubLevels");
                Object subLevelsObj = getAll.invoke(container);
                if (subLevelsObj instanceof Iterable<?> subLevels) {
                    for (Object sl : subLevels) {
                        if (sl != null && checkSubLevelHigh(sl, pos)) return true;
                    }
                }
            } else {
                Method getSubLevels = sableClass.getMethod("getSubLevels", Level.class);
                Object subLevelsObj = getSubLevels.invoke(null, level);
                if (subLevelsObj instanceof Iterable<?> subLevels) {
                    for (Object sl : subLevels) {
                        if (sl != null && checkSubLevelHigh(sl, pos)) return true;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static boolean checkSubLevelHigh(Object subLevel, BlockPos pos) {
        try {
            Method boxMethod = subLevel.getClass().getMethod("getWorldAABB");
            Object boxObj = boxMethod.invoke(subLevel);
            if (boxObj instanceof AABB aabb && aabb.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)) {
                return aabb.maxY > 400.0;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}