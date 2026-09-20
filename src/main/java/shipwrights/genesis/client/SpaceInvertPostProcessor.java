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
import shipwrights.genesis.space.Celestial;
import shipwrights.genesis.space.SpaceLevel;
import shipwrights.genesis.space.type.BuiltinCelestialTypes;

import java.util.function.Predicate;

public class SpaceInvertPostProcessor {
    public static final SpaceInvertPostProcessor INSTANCE = new SpaceInvertPostProcessor();

    private PostChain postChain;

    public ResourceLocation getPostChainLocation() {
        return ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "space_invert");
    }

    public void setPostChain(PostChain postChain) {
        this.postChain = postChain;
    }

    public PostChain getPostChain() {
        return this.postChain;
    }

    public void beforeProcess(PoseStack viewModelStack) {
        if (this.postChain == null) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer localPlayer = mc.player;
        if (level == null || localPlayer == null) {
            return;
        }

        Long ticks = NeoGenesisMod.getTicks(level);
        Float partialTick = Float.valueOf(mc.getTimer().getGameTimeDeltaPartialTick(Boolean.TRUE));
        Vec3 camPos = localPlayer.getPosition(partialTick.floatValue());
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

        Float camX = Float.valueOf((float) camPos.x);
        Float camY = Float.valueOf((float) camPos.y);
        Float camZ = Float.valueOf((float) camPos.z);
        Float starX = Float.valueOf(starPos != null ? (float) starPos.x() : 0.0F);
        Float starY = Float.valueOf(starPos != null ? (float) starPos.y() : 0.0F);
        Float starZ = Float.valueOf(starPos != null ? (float) starPos.z() : 0.0F);

        for (PostPass pass : this.postChain.passes) {
            Uniform cameraUniform = pass.getEffect().getUniform("cameraPos");
            if (cameraUniform != null) {
                cameraUniform.set(camX.floatValue(), camY.floatValue(), camZ.floatValue());
            }
            Uniform uniform = pass.getEffect().getUniform("starPos");
            if (uniform != null) {
                uniform.set(starX.floatValue(), starY.floatValue(), starZ.floatValue());
            }
        }
    }
}