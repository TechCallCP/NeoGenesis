package shipwrights.genesis.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import org.spongepowered.asm.mixin.Mixin;
import shipwrights.genesis.NeoGenesisMod;

@EventBusSubscriber(modid = NeoGenesisMod.MOD_ID)
@Mixin(Entity.class)
public class SableEntitySpaceHandler {

    @SubscribeEvent
    private static void onEntityTick(EntityTickEvent.Pre event) {
        Entity entity = event.getEntity();
        Level level = entity.level();

        // Only process in Genesis space/subspace dimensions
        if (!level.isClientSide && NeoGenesisMod.shouldCancelVoidDamage(level)) {
            // Check if entity is riding or near a Sable/Create contraption ship
            if (entity.getVehicle() instanceof AbstractContraptionEntity contraption) {
                // Keep entity active / reset void tick counters
                entity.fallDistance = 0.0f;
            } else {
                // Search for nearby contraption bounding boxes
                AABB searchBox = entity.getBoundingBox().inflate(2.0);
                boolean nearContraption = level.getEntitiesOfClass(
                        AbstractContraptionEntity.class,
                        searchBox
                ).stream().anyMatch(c -> c.getBoundingBox().contains(entity.position()));

                if (nearContraption) {
                    entity.fallDistance = 0.0f;
                }
            }
        }
    }
}
