package shipwrights.genesis.content.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import shipwrights.genesis.NeoGenesisMod;

public class GenesisSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, NeoGenesisMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_ENGINE_START = registerSound("void_engine_start");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_ENGINE_TRAVEL = registerSound("void_engine_travel");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_ENGINE_STOP = registerSound("void_engine_stop");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORMHOLE_AMBIANCE = registerSound("wormhole_ambiance");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIASMA_HISS = registerSound("miasma_hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_CORE_ORE_SOUND = registerSound("void_core_ore_sound");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () ->
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, name)));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}