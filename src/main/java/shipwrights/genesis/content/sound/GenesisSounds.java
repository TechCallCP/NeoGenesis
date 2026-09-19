package shipwrights.genesis.content.sound;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import shipwrights.genesis.NeoGenesisMod;

public class GenesisSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, NeoGenesisMod.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_ENGINE_START = SOUND_EVENTS.register("void_engine_start",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "void_engine_start")));

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_ENGINE_TRAVEL = SOUND_EVENTS.register("void_engine_travel",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "void_engine_travel")));

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_ENGINE_STOP = SOUND_EVENTS.register("void_engine_stop",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "void_engine_stop")));

    public static final DeferredHolder<SoundEvent, SoundEvent> WORMHOLE_AMBIANCE = SOUND_EVENTS.register("wormhole_ambiance",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "wormhole_ambiance")));

    public static final DeferredHolder<SoundEvent, SoundEvent> MIASMA_HISS = SOUND_EVENTS.register("miasma_hiss",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "miasma_hiss")));

    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_CORE_ORE_SOUND = SOUND_EVENTS.register("void_core_ore_sound",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "void_core_ore_sound")));
}
