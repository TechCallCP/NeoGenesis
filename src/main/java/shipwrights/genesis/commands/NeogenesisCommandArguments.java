package shipwrights.genesis.commands;

import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import shipwrights.genesis.NeoGenesisMod;

public class NeogenesisCommandArguments {
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES =
            DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, NeoGenesisMod.MOD_ID);

    public static final DeferredHolder<ArgumentTypeInfo<?, ?>, ArgumentTypeInfo<CelestialArgument, SingletonArgumentInfo<CelestialArgument>.Template>> CELESTIAL =
            COMMAND_ARGUMENT_TYPES.register("celestial", () ->
                    SingletonArgumentInfo.contextFree(CelestialArgument::celestial)
            );

    public static void register(IEventBus eventBus) {
        COMMAND_ARGUMENT_TYPES.register(eventBus);
    }
}
