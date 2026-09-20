package shipwrights.genesis.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

import org.lwjgl.openal.AL10;

import shipwrights.genesis.NeoGenesisMod;

public class SoundFilterHandler {

    private static final Minecraft CLIENT = Minecraft.getInstance();

    public static int determineFilterId(SoundInstance sound) {
        if (!AudioFilterManager.isReady() || CLIENT.player == null || CLIENT.level == null) {
            return AL10.AL_NONE;
        }

        if (shouldExcludeFromFiltering(sound)) {
            return AL10.AL_NONE;
        }

        return getSpaceFilterId();
    }

    private static int getWaterFilterId() {
        if (CLIENT.player == null) {
            return AL10.AL_NONE;
        }

        if (CLIENT.player.isUnderWater()) {
            return AudioFilterManager.getFilterId(AudioFilterManager.Filter.LOWPASS);
        }

        return AL10.AL_NONE;
    }

    private static boolean shouldExcludeFromFiltering(SoundInstance sound) {
        return sound.getSource() == SoundSource.MUSIC ||
                sound.getSource() == SoundSource.WEATHER ||
                sound.getSource() == SoundSource.MASTER;
    }

    private static int getSpaceFilterId() {
        if (CLIENT.player == null || CLIENT.level == null) {
            return AL10.AL_NONE;
        }

        if (NeoGenesisMod.isSpaceDimension(CLIENT.level)) {
            return AudioFilterManager.getFilterId(AudioFilterManager.Filter.LOWPASS);
        }

        return AL10.AL_NONE;
    }
}