package shipwrights.genesis.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import shipwrights.genesis.NeoGenesisMod;

@Mixin(TimeCommand.class)
public class TimeCommandMixin {

    @WrapMethod(method = "getDayTime", remap = false)
    private static int getDayTimeWrap(ServerLevel level, Operation<Integer> original) {
        return Math.toIntExact(Math.min(Integer.MAX_VALUE, NeoGenesisMod.getTicks(level)));
    }

    @WrapOperation(method = "addTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getDayTime()J"), remap = false)
    private static long fixAddTime(ServerLevel instance, Operation<Long> original) {
        return NeoGenesisMod.getTicks(instance);
    }
}