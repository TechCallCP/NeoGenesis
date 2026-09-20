package shipwrights.genesis.space;

import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;

/**
 * Represents where the observer is in space, for purposes such as rendering celestials.
 */
public interface VantagePoint {

    Vector3dc getPosition();
    Quaterniondc getRotation();

    /**
     * If null, the observer has no access to space (e.g. in The Nether).
     */
    static @Nullable VantagePoint get(Level level, Vector3dc posInLevel, long ticks, float partialTick) {
        if (NeoGenesisMod.isSpaceDimension(level)) {
            return new VantagePoint.InSpace();
        } else {
            Celestial celestial = NeoGenesisMod.getCelestialForLevel(level);
            if (celestial != null) {
                Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(level);
                Quaterniond rotation = new Quaterniond()
                        .rotateTo(new Vector3d(0, 1, 0), new Vector3d(0, 0, -1));

                return new VantagePoint.OnCelestial(celestial, rotation, ticks, partialTick, registry);
            } else {
                return null;
            }
        }
    }

    record InSpace() implements VantagePoint {
        @Override
        public Vector3dc getPosition() {
            return new Vector3d();
        }

        @Override
        public Quaterniondc getRotation() {
            return new Quaterniond();
        }
    }

    record OnCelestial(
            Celestial celestial,
            Quaterniondc cameraRotationFromNorthPole,
            long ticks,
            float partialTick,
            Registry<Celestial> registry
    ) implements VantagePoint {

        @Override
        public Vector3dc getPosition() {
            return celestial.getPosition(ticks, partialTick, registry);
        }

        @Override
        public Quaterniondc getRotation() {
            return getCelestialRotation().mul(cameraRotationFromNorthPole, new Quaterniond());
        }

        public Quaterniondc getCelestialRotation() {
            return celestial.getRotation(ticks, partialTick, registry);
        }
    }
}
