package shipwrights.genesis.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

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

@Mixin(value = ServerLevel.class, remap = false)
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

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void addEntityMixin(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        NeoGenesisMod.refreshEntityScaling(entity, getLevel());
    }

    @Inject(method = "addNewPlayer", at = @At("HEAD"))
    private void addPlayerMixin(ServerPlayer player, CallbackInfo ci) {
        NeoGenesisMod.refreshEntityScaling(player, getLevel());
    }
}