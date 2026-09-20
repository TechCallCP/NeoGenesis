package shipwrights.genesis.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import shipwrights.genesis.NeoGenesisMod;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoGenesisMod.MOD_ID)
public class ShaderRegistry {

    public static ShaderInstance SUN_SHADER;
    public static ShaderInstance BLACKHOLE_SHADER;
    public static ShaderInstance PLANET_SHADER;
    public static ShaderInstance PLANET_ATMOSPHERE_SHADER;
    public static ShaderInstance PLANET_TEXTURED_SHADER;
    public static ShaderInstance PLANET_MASK_SHADER;
    public static ShaderInstance PLANET_SHADOW_SHADER;
    public static ShaderInstance WORMHOLE_SHADER;
    public static ShaderInstance STAR_GLOW_SHADER;

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "sun"), DefaultVertexFormat.POSITION_COLOR),
                shader -> SUN_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "blackhole"), DefaultVertexFormat.POSITION_COLOR),
                shader -> BLACKHOLE_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "planet"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shader -> PLANET_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "planet_atmosphere"), DefaultVertexFormat.POSITION_COLOR),
                shader -> PLANET_ATMOSPHERE_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "planet_textured"), DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL),
                shader -> PLANET_TEXTURED_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "planet_mask"), DefaultVertexFormat.POSITION_COLOR),
                shader -> PLANET_MASK_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "planet_shadow"), DefaultVertexFormat.POSITION_COLOR),
                shader -> PLANET_SHADOW_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "wormhole"), DefaultVertexFormat.POSITION_TEX_COLOR),
                shader -> WORMHOLE_SHADER = shader
        );
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "star_glow"), DefaultVertexFormat.POSITION_COLOR),
                shader -> STAR_GLOW_SHADER = shader
        );
    }

    private static RenderType SUN_RENDER_TYPE;
    private static RenderType BLACKHOLE_RENDER_TYPE;
    private static RenderType PLANET_RENDER_TYPE;
    private static RenderType PLANET_ATMOSPHERE_RENDER_TYPE;
    private static RenderType PLANET_MASK_RENDER_TYPE;
    private static RenderType PLANET_SHADOW_RENDER_TYPE;
    private static final ConcurrentHashMap<ResourceLocation, RenderType> TEXTURED_PLANET_RENDER_TYPES = new ConcurrentHashMap<>();

    public static RenderType getSunRenderType() {
        if (SUN_RENDER_TYPE == null) {
            SUN_RENDER_TYPE = RenderType.create(
                    "sun_render_type",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> SUN_SHADER))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                            .setCullState(RenderStateShard.CULL)
                            .createCompositeState(false)
            );
        }
        return SUN_RENDER_TYPE;
    }

    public static RenderType getBlackholeRenderType() {
        if (BLACKHOLE_RENDER_TYPE == null) {
            BLACKHOLE_RENDER_TYPE = RenderType.create(
                    "blackhole_render_type",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> BLACKHOLE_SHADER))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                            .setCullState(RenderStateShard.CULL)
                            .createCompositeState(false)
            );
        }
        return BLACKHOLE_RENDER_TYPE;
    }

    public static RenderType getPlanetRenderType() {
        if (PLANET_RENDER_TYPE == null) {
            PLANET_RENDER_TYPE = RenderType.create(
                    "planet_render_type",
                    DefaultVertexFormat.POSITION_TEX_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> PLANET_SHADER))
                            .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                            .setCullState(RenderStateShard.CULL)
                            .createCompositeState(false)
            );
        }
        return PLANET_RENDER_TYPE;
    }

    public static RenderType getPlanetAtmosphereRenderType() {
        if (PLANET_ATMOSPHERE_RENDER_TYPE == null) {
            PLANET_ATMOSPHERE_RENDER_TYPE = RenderType.create(
                    "planet_atmosphere_render_type",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> PLANET_ATMOSPHERE_SHADER))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                            .setCullState(RenderStateShard.CULL)
                            .createCompositeState(false)
            );
        }
        return PLANET_ATMOSPHERE_RENDER_TYPE;
    }

    public static RenderType getPlanetMaskRenderType() {
        if (PLANET_MASK_RENDER_TYPE == null) {
            PLANET_MASK_RENDER_TYPE = RenderType.create(
                    "planet_mask_render_type",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> PLANET_MASK_SHADER))
                            .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                            .setCullState(RenderStateShard.CULL)
                            .createCompositeState(false)
            );
        }
        return PLANET_MASK_RENDER_TYPE;
    }

    public static RenderType getPlanetShadowRenderType() {
        if (PLANET_SHADOW_RENDER_TYPE == null) {
            PLANET_SHADOW_RENDER_TYPE = RenderType.create(
                    "planet_shadow_render_type",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.TRIANGLES,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> PLANET_SHADOW_SHADER))
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                            .setCullState(RenderStateShard.CULL)
                            .createCompositeState(false)
            );
        }
        return PLANET_SHADOW_RENDER_TYPE;
    }

    public static RenderType getTexturedPlanetRenderType(ResourceLocation textureLocation) {
        return TEXTURED_PLANET_RENDER_TYPES.computeIfAbsent(textureLocation, loc -> {
            ResourceLocation fullTexturePath = ResourceLocation.fromNamespaceAndPath(
                    loc.getNamespace(),
                    "textures/" + loc.getPath() + ".png"
            );

            return RenderType.create(
                    "planet_textured_" + loc.getNamespace() + "_" + loc.getPath().replace("/", "_"),
                    DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(new RenderStateShard.ShaderStateShard(() -> PLANET_TEXTURED_SHADER))
                            .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                            .setCullState(RenderStateShard.CULL)
                            .setTextureState(new RenderStateShard.TextureStateShard(fullTexturePath, false, false))
                            .createCompositeState(false)
            );
        });
    }

    public static void clearTexturedPlanetRenderTypes() {
        TEXTURED_PLANET_RENDER_TYPES.clear();
    }
}