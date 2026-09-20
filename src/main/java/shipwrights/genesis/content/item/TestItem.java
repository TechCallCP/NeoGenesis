package shipwrights.genesis.content.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.SpaceLevel;

public class TestItem extends Item {

    public TestItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel() instanceof ServerLevel serverLevel && context.getHand() == InteractionHand.MAIN_HAND && context.getPlayer() != null) {
            ray(serverLevel, context.getPlayer(), context.getHand());
        }
        return super.useOn(context);
    }

    public static void ray(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND) {
            Vec3 forward = player.getForward();

            Vector3d origin = new Vector3d(player.getX(), player.getY(), player.getZ());
            Vector3d direction = new Vector3d(forward.x, forward.y, forward.z);
            Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(level);

            Pair<Celestial, Double> result = SpaceLevel.celestialRaycast(registry, NeoGenesisMod.getTicks(level), 0.0F, origin, direction, celestialType -> true);
            if (result != null) {
                player.sendSystemMessage(Component.literal("BODY FOUND: " + registry.getResourceKey(result.getFirst()).orElseThrow().location()));
                Vector3dc pos = result.getFirst().getPosition(NeoGenesisMod.getTicks(level), registry);
                player.sendSystemMessage(Component.literal("Position: " + (int) pos.x() + " " + (int) pos.y() + " " + (int) pos.z()));
            }
        }
    }
}
