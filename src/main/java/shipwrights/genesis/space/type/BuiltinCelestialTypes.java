package shipwrights.genesis.space.type;

import com.mojang.serialization.Codec;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.space.properties.CelestialProperties;
import shipwrights.genesis.space.properties.EmptyProperties;
import shipwrights.genesis.space.properties.PlanetProperties;
import shipwrights.genesis.space.properties.StarProperties;
import shipwrights.genesis.space.renderer.BlackholeRenderer;
import shipwrights.genesis.space.renderer.CelestialRenderer;
import shipwrights.genesis.space.renderer.PlanetRenderer;
import shipwrights.genesis.space.renderer.StarRenderer;

public class BuiltinCelestialTypes {

    public static final CelestialType STAR = new CelestialType() {
        @Override
        public boolean castsLight() { return true; }

        @Override
        public boolean castsShadow() { return false; }

        @Override
        public boolean isVisitable() { return false; }

        private CelestialRenderer renderer = null;

        @Override
        public @NotNull CelestialRenderer getRenderer() {
            if (renderer == null) renderer = new StarRenderer();
            return renderer;
        }

        @Override
        public @NotNull ResourceLocation getID() {
            return ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "star");
        }

        @Override
        public @NotNull Codec<? extends CelestialProperties> propertiesCodec() {
            return StarProperties.CODEC;
        }
    };

    public static final CelestialType BODY = new CelestialType() {
        @Override
        public boolean castsLight() { return false; }

        @Override
        public boolean castsShadow() { return true; }

        @Override
        public boolean isVisitable() { return true; }

        private CelestialRenderer renderer = null;

        @Override
        public @NotNull CelestialRenderer getRenderer() {
            if (renderer == null) renderer = new PlanetRenderer();
            return renderer;
        }

        @Override
        public @NotNull ResourceLocation getID() {
            return ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "body");
        }

        @Override
        public @NotNull Codec<? extends CelestialProperties> propertiesCodec() {
            return PlanetProperties.CODEC;
        }
    };

    public static final CelestialType BLACKHOLE = new CelestialType() {
        @Override
        public boolean castsLight() { return false; }

        @Override
        public boolean castsShadow() { return false; }

        @Override
        public boolean isVisitable() { return false; }

        private CelestialRenderer renderer = null;

        @Override
        public @NotNull CelestialRenderer getRenderer() {
            if (renderer == null) renderer = new BlackholeRenderer();
            return renderer;
        }

        @Override
        public @NotNull ResourceLocation getID() {
            return ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "blackhole");
        }

        @Override
        public @NotNull Codec<? extends CelestialProperties> propertiesCodec() {
            return EmptyProperties.CODEC;
        }
    };

    public static void register() {
        CelestialType.register(STAR);
        CelestialType.register(BODY);
        CelestialType.register(BLACKHOLE);
    }
}
