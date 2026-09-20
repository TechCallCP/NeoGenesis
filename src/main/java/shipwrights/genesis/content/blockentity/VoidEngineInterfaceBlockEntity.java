package shipwrights.genesis.content.blockentity;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

import org.slf4j.Logger;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.content.block.VoidCoreBlock;
import shipwrights.genesis.networking.GenesisNetworking;
import shipwrights.genesis.networking.StopVoidEngineStartSoundPacket;

import shipwrights.genesis.networking.VoidEngineSoundPacket;
import shipwrights.genesis.networking.WormholeTravelSoundPacket;

public class VoidEngineInterfaceBlockEntity extends BlockEntity {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation WORMHOLE_DIM = ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "wormhole");

    private static final int MAX_ENERGY = 8192;
    private static final int ENERGY_PER_TICK = 512;
    private final EnergyStorage energyStorage = new EnergyStorage(MAX_ENERGY);

    public VoidEngineInterfaceBlockEntity(BlockPos pos, BlockState state) {
        super(GenesisBlockEntities.VOID_ENGINE_INTERFACE.get(), pos, state);
    }

    private int chargeUpTicks = 0;
    private boolean active = false;
    private ResourceLocation returningDim = ResourceLocation.fromNamespaceAndPath("minecraft", "overworld");

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("energy")) {
            energyStorage.deserializeNBT(registries, tag.get("energy"));
        }
        chargeUpTicks = tag.getInt("chargeUpTicks");
        active = tag.getBoolean("active");
        if (tag.contains("returningDim")) {
            returningDim = ResourceLocation.parse(tag.getString("returningDim"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("energy", energyStorage.serializeNBT(registries));
        tag.putInt("chargeUpTicks", chargeUpTicks);
        tag.putBoolean("active", active);
        tag.putString("returningDim", returningDim.toString());
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
        if (blockEntity instanceof VoidEngineInterfaceBlockEntity voidEngineInterface) {
            if (voidEngineInterface.energyStorage.getEnergyStored() < voidEngineInterface.energyStorage.getMaxEnergyStored()) {
                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = pos.relative(direction);
                    IEnergyStorage energy = level.getCapability(Capabilities.EnergyStorage.BLOCK, neighborPos, direction.getOpposite());
                    if (energy != null && energy.canExtract()) {
                        int toExtract = Math.min(128, voidEngineInterface.energyStorage.getMaxEnergyStored() - voidEngineInterface.energyStorage.getEnergyStored());
                        int extracted = energy.extractEnergy(toExtract, false);
                        voidEngineInterface.energyStorage.receiveEnergy(extracted, false);
                    }
                }
            }

            BlockState core = level.getBlockState(pos.offset(state.getValue(BlockStateProperties.HORIZONTAL_FACING).getNormal().multiply(-1)));
            if (core.hasProperty(VoidCoreBlock.DORMANT) && !core.getValue(VoidCoreBlock.DORMANT)) {
                Vec3 center = pos.getCenter();
                boolean isPowered = level.getBlockState(pos).hasProperty(BlockStateProperties.POWERED) && level.getBlockState(pos).getValue(BlockStateProperties.POWERED);
                boolean hasEnergy = voidEngineInterface.energyStorage.getEnergyStored() >= ENERGY_PER_TICK;

                if (isPowered && hasEnergy) {
                    voidEngineInterface.energyStorage.extractEnergy(ENERGY_PER_TICK, false);

                    if (!voidEngineInterface.active && voidEngineInterface.chargeUpTicks >= 0) {
                        voidEngineInterface.active = true;
                        LOGGER.info("Current dimension id: {}", level.dimension().location());
                        if (!level.dimension().location().equals(WORMHOLE_DIM)) {
                            GenesisNetworking.sendToAll(new StopVoidEngineStartSoundPacket());
                            GenesisNetworking.sendToAll(new VoidEngineSoundPacket(pos));
                        }
                    }

                    if (!level.dimension().location().equals(WORMHOLE_DIM) && level.getServer() != null) {
                        voidEngineInterface.chargeUpTicks++;

                        if (voidEngineInterface.chargeUpTicks == 244) {
                            if (!level.dimension().location().equals(ResourceLocation.fromNamespaceAndPath(NeoGenesisMod.MOD_ID, "great_unknown"))) {
                                if (level.getBlockState(pos).hasProperty(BlockStateProperties.POWERED) && level.getBlockState(pos).getValue(BlockStateProperties.POWERED)) {
                                    explode(level, center);
                                }
                            } else {
                                voidEngineInterface.chargeUpTicks = 2;
                                voidEngineInterface.returningDim = level.dimension().location();

                                ServerLevel wormholeLevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, WORMHOLE_DIM));
                                if (wormholeLevel != null) {
                                    GenesisNetworking.sendToAll(new WormholeTravelSoundPacket(pos));
                                }
                                return;
                            }
                        }
                    } else {
                        voidEngineInterface.chargeUpTicks = 32;
                    }
                } else {
                    GenesisNetworking.sendToAll(new StopVoidEngineStartSoundPacket());
                    if (voidEngineInterface.chargeUpTicks > 0) {
                        voidEngineInterface.chargeUpTicks--;
                    }
                    if (level.dimension().location().equals(WORMHOLE_DIM) && level.getServer() != null) {
                        if (voidEngineInterface.chargeUpTicks <= 0) {
                            voidEngineInterface.chargeUpTicks = -64;
                            ServerLevel returnLevel = level.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, voidEngineInterface.returningDim));
                            returnFromWormhole(level, pos, returnLevel, false);
                        }
                    } else {
                        if (voidEngineInterface.chargeUpTicks > 0) {
                            voidEngineInterface.chargeUpTicks = 0;
                            GenesisNetworking.sendToAll(new StopVoidEngineStartSoundPacket());
                        }
                    }
                }
            }
            if (voidEngineInterface.chargeUpTicks < 0) {
                voidEngineInterface.chargeUpTicks++;
            }
            if (voidEngineInterface.active) {
                if (voidEngineInterface.chargeUpTicks == 0) {
                    voidEngineInterface.active = false;
                }
            }
        }
    }

    public static void returnFromWormhole(Level level, BlockPos pos, ServerLevel returnLevel, boolean unstable) {
        if (returnLevel != null) {
            Vec3 targetPos = pos.getCenter();

            GenesisNetworking.sendToAll(new WormholeTravelSoundPacket(pos));
            if (unstable) {
                explode(returnLevel, targetPos);
            }
        }
    }

    private static void explode(Level level, Vec3 center) {
        level.explode(null, center.x, center.y, center.z, 16.0F, Level.ExplosionInteraction.BLOCK);
    }
}
