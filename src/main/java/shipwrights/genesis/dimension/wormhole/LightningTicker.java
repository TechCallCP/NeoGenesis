package shipwrights.genesis.dimension.wormhole;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;

// NeoForge event imports
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import shipwrights.genesis.NeoGenesisMod;

import java.util.List;

@EventBusSubscriber(modid = NeoGenesisMod.MOD_ID)
public class LightningTicker {

    private static int ticks = 0;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLevelTick(final LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (!serverLevel.players().isEmpty() && NeoGenesisMod.WORMHOLE_DIM.equals(serverLevel.dimension().location())) {
                wormholeLightningTick(serverLevel);
            }
        }
    }

    private static void wormholeLightningTick(ServerLevel wormholeLevel) {
        ticks++;

        if (ticks >= 20) {
            ticks = 0;

            if (wormholeLevel.random.nextFloat() < 0.05f) {
                List<ServerPlayer> players = wormholeLevel.players();
                if (!players.isEmpty()) {
                    ServerPlayer randomPlayer = players.get(wormholeLevel.random.nextInt(players.size()));

                    double distance = 30 + wormholeLevel.random.nextDouble() * 50;
                    double angle = wormholeLevel.random.nextDouble() * Math.PI * 2;

                    Vec3 playerPos = randomPlayer.position();
                    double offsetX = Math.cos(angle) * distance;
                    double offsetZ = Math.sin(angle) * distance;

                    BlockPos lightningPos = BlockPos.containing(
                            playerPos.x + offsetX,
                            playerPos.y + wormholeLevel.random.nextInt(-25, 25),
                            playerPos.z + offsetZ
                    );

                    LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(wormholeLevel);
                    if (lightning != null) {
                        lightning.moveTo(Vec3.atBottomCenterOf(lightningPos));
                        lightning.setVisualOnly(true);
                        wormholeLevel.addFreshEntity(lightning);
                    }
                }
            }
        }
    }
}
