package shipwrights.genesis.teleportation.integration;

import net.neoforged.bus.api.Event;
import shipwrights.genesis.space.Celestial;

/// Fired when a sub-level/ship is denied entry to a planet due to addon or datapack functionality
public final class TeleportDisallowedEvent extends Event {
    private final Object ship;
    private final Celestial celestial;

    public TeleportDisallowedEvent(Object ship, Celestial celestial) {
        this.ship = ship;
        this.celestial = celestial;
    }

    /// Sub-level or ship object that tried to teleport to a Celestial
    public Object ship() {
        return ship;
    }

    /// Celestial that the ship tried to teleport to
    public Celestial celestial() {
        return celestial;
    }
}