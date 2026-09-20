package shipwrights.genesis.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.mixin.PostChainAccessor;
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.SpaceLevel;
import shipwrights.genesis.space.type.BuiltinCelestialTypes;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;

public class SpaceInvertPostProcessor {
    public static final SpaceInvertPostProcessor INSTANCE = new SpaceInvertPostProcessor();

    private PostChain postChain;
    private boolean active;

    public ResourceLocation getPostChainLocation() {
        return ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "space_invert");
    }

    public void setPostChain(PostChain postChain) {
        this.postChain = postChain;
    }

    public PostChain getPostChain() {
        return this.postChain;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void beforeProcess(PoseStack viewModelStack) {
        if (!this.active || this.postChain == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer localPlayer = mc.player;
        if (level == null || localPlayer == null) {
            return;
        }

        long ticks = NeoGenesisMod.getTicks(level);
        float partialTick = mc.getTimer().getGameTimeDeltaPartialTick(true);
        Vec3 camPos = localPlayer.getPosition(partialTick);
        Vector3d camPosJoml = new Vector3d(camPos.x, camPos.y, camPos.z);

        Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(level);
        Vector3dc starPos = null;
        Pair<Celestial, Double> result = SpaceLevel.nearestCelestialWhere(
                registry,
                camPosJoml,
                ticks,
                partialTick,
                Predicate.isEqual(BuiltinCelestialTypes.STAR)
        );
        if (result != null) {
            starPos = result.getFirst().getPosition(ticks, partialTick, registry);
        }

        float camX = (float) camPos.x;
        float camY = (float) camPos.y;
        float camZ = (float) camPos.z;
        float starX = starPos != null ? (float) starPos.x() : 0.0F;
        float starY = starPos != null ? (float) starPos.y() : 0.0F;
        float starZ = starPos != null ? (float) starPos.z() : 0.0F;

        List<PostPass> passes = getPostChainPasses(this.postChain);
        passes.forEach(pass -> {
            Uniform cameraUniform = pass.getEffect().getUniform("cameraPos");
            if (cameraUniform != null) {
                cameraUniform.set(camX, camY, camZ);
            }
            Uniform uniform = pass.getEffect().getUniform("starPos");
            if (uniform != null) {
                uniform.set(starX, starY, starZ);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<PostPass> getPostChainPasses(PostChain postChain) {
        if (postChain instanceof PostChainAccessor accessor) {
            return accessor.getPasses();
        }
        try {
            Field field = PostChain.class.getDeclaredField("passes");
            field.setAccessible(true);
            return (List<PostPass>) field.get(postChain);
        } catch (Exception e) {
            return List.of();
        }
    }
}