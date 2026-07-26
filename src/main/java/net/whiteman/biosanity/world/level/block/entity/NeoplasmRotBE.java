package net.whiteman.biosanity.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.whiteman.biosanity.world.level.block.NeoplasmRotBlock;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceType;
import net.whiteman.biosanity.world.level.block.NeoplasmVeinBlock;
import net.whiteman.biosanity.client.resources.model.ModelProperties;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.whiteman.biosanity.world.level.block.NeoplasmVeinBlock.HAS_NUTRIENT;
import static net.whiteman.biosanity.world.level.neoplasm.common.NeoplasmConstants.DIRECTIONS;

public class NeoplasmRotBE extends BlockEntity {
    public static final int TICKS_TO_TRANSFER_NUTRIENT = 15;
    public static final float EXTRACTION_PERCENT = 0.5f;

    private BlockState originalState = Blocks.AIR.defaultBlockState();
    private int infectionStage = 0;

    private final Map<ResourceType, Integer> heldResources = new HashMap<>(2);
    private int transferCooldown = 0;

    public final Map<ResourceType, Integer> resourceData = new HashMap<>(2);

    public NeoplasmRotBE(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEOPLASM_ROT_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmRotBE be) {
        if (state.getValue(HAS_NUTRIENT) || !be.heldResources.isEmpty()) {
            // Transfer countdown
            if (be.transferCooldown > 0) {
                be.transferCooldown--;
                return;
            }

            be.transferResource(level, pos, state);
        }
    }

    // Resource transfer
    // Transfers resources to veins/other rots using "chain" and "dijkstra algorithm" method
    // cannot transfer resource directly to core
    private void transferResource(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
        int myDistance = state.getValue(NeoplasmRotBlock.DISTANCE);

        for (Direction dir : DIRECTIONS) {
            BlockPos targetPos = pos.relative(dir);
            BlockState targetState = level.getBlockState(targetPos);
            BlockEntity targetBE = level.getBlockEntity(targetPos);

            // First priority is transfer to the closest vein
            if (targetState.getBlock() instanceof NeoplasmVeinBlock) {
                // If vein has resource, do nothing and request update later
                if (targetState.getValue(NeoplasmVeinBlock.HAS_NUTRIENT)) {
                    level.scheduleTick(pos, state.getBlock(), TICKS_TO_TRANSFER_NUTRIENT);
                    return;
                }

                // Target vein
                level.setBlock(targetPos, targetState.setValue(NeoplasmVeinBlock.HAS_NUTRIENT, true), Block.UPDATE_ALL);

                if (targetBE instanceof NeoplasmVeinBE veinBE) {
                    // Target vein
                    veinBE.setHeldData(this.heldResources);
                    veinBE.setNutrientTransferCooldown(TICKS_TO_TRANSFER_NUTRIENT);
                    // Current rot
                    level.setBlock(pos, state.setValue(HAS_NUTRIENT, false), Block.UPDATE_ALL);
                    this.clearHeld();
                }
                break;
            }
            // Second priority is transfer to rot block that placed close to vein than us
            else if (targetState.getBlock() instanceof NeoplasmRotBlock) {
                int targetDist = targetState.getValue(NeoplasmRotBlock.DISTANCE);

                if (targetDist < myDistance) {
                    // If rot has resource to send, do nothing and request update later
                    if (targetState.getValue(HAS_NUTRIENT)) {
                        level.scheduleTick(pos, state.getBlock(), TICKS_TO_TRANSFER_NUTRIENT);
                        return;
                    }

                    // Target rot
                    level.setBlock(targetPos, targetState.setValue(HAS_NUTRIENT, true), Block.UPDATE_ALL);

                    if (targetBE instanceof NeoplasmRotBE nextRotBE) {
                        // Target rot
                        nextRotBE.setResourceData(this.heldResources);
                        nextRotBE.transferCooldown = TICKS_TO_TRANSFER_NUTRIENT;
                        // Current rot
                        level.setBlock(pos, state.setValue(HAS_NUTRIENT, false), Block.UPDATE_ALL);
                        this.clearHeld();
                    }


                    // TEST PARTICLE
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.ELECTRIC_SPARK,
                                targetPos.getX() + 0.5,
                                targetPos.getY() + 0.7,
                                targetPos.getZ() + 0.5,
                                10,
                                0.4, 0.4, 0.4,
                                0.05
                        );
                    }
                    break;
                }
            }
        }
    }

    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(ModelProperties.ORIGINAL_STATE, originalState)
                .with(ModelProperties.OVERLAY_STAGE, infectionStage)
                .build();
    }

    public void setHeldData() {
        Map<ResourceType, Integer> extractedResources = new HashMap<>(2);

        for (Map.Entry<ResourceType, Integer> entry : resourceData.entrySet()) {
            int amount = entry.getValue();
            int extractedAmount = (int) (amount * EXTRACTION_PERCENT);
            int remain = amount - extractedAmount;

            resourceData.replace(entry.getKey(), remain);
            extractedResources.put(entry.getKey(), extractedAmount);
        }

        this.heldResources.putAll(extractedResources);
        this.setChanged();
    }

    private void clearHeld() {
        this.heldResources.clear();
        this.setChanged();
    }

    public void setResourceData(Map<ResourceType, Integer> data) {
        this.resourceData.putAll(data);
        this.setChanged();
    }

    public int getInfectionStage() {
        return infectionStage;
    }

    public void setInfectionStage(int stage) {
        this.infectionStage = stage;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void setOriginalState(BlockState state) {
        this.originalState = state;
        this.setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public BlockState getOriginalState() {
        return this.originalState;
    }

    public float getMultiplier(float[] multipliers) {
        if (multipliers == null || multipliers.length == 0) return 1.0f;

        int index = Math.min(this.infectionStage, multipliers.length - 1);
        return multipliers[Math.max(0, index)];
    }

    public double getMultiplier(double[] multipliers) {
        if (multipliers == null || multipliers.length == 0) return 1.0f;

        int index = Math.min(this.infectionStage, multipliers.length - 1);
        return multipliers[Math.max(0, index)];
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        tag.put("OriginalBlock", NbtUtils.writeBlockState(originalState));
        tag.putInt("overlayStage", infectionStage);

        ListTag heldResourceList = new ListTag();
        for (Map.Entry<ResourceType, Integer> entry : heldResources.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("key", entry.getKey().name());
            entryTag.putInt("value", entry.getValue());

            heldResourceList.add(entryTag);
        }
        tag.put("held_resource", heldResourceList);

        ListTag resourceDataList = new ListTag();
        for (Map.Entry<ResourceType, Integer> entry : resourceData.entrySet()) {
            CompoundTag entryTag = new CompoundTag();

            entryTag.putString("key", entry.getKey().name());
            entryTag.putInt("value", entry.getValue());

            resourceDataList.add(entryTag);
        }
        tag.put("resource_data_list", resourceDataList);

        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OriginalBlock", Tag.TAG_COMPOUND)) {
            try {
                HolderGetter<Block> holdergetter = this.level != null ?
                        this.level.holderLookup(Registries.BLOCK) :
                        BuiltInRegistries.BLOCK.asLookup();

                this.originalState = NbtUtils.readBlockState(holdergetter, tag.getCompound("OriginalBlock"));
            } catch (IllegalArgumentException e) {
                this.originalState = Blocks.AIR.defaultBlockState();
            }
        }
        this.infectionStage = tag.getInt("overlayStage");

        this.heldResources.clear();
        ListTag heldResourceList = tag.getList("held_resource", Tag.TAG_COMPOUND);
        for (Tag pTag : heldResourceList) {
            if (pTag instanceof CompoundTag entryTag) {
                ResourceType key = ResourceType.valueOf(entryTag.getString("key"));
                int value = entryTag.getInt("value");

                this.heldResources.put(key, value);
            }
        }

        this.resourceData.clear();
        ListTag resourceDataList = tag.getList("resource_data_list", Tag.TAG_COMPOUND);
        for (Tag pTag : resourceDataList) {
            if (pTag instanceof CompoundTag entryTag) {
                ResourceType key = ResourceType.valueOf(entryTag.getString("key"));
                int value = entryTag.getInt("value");

                this.resourceData.put(key, value);
            }
        }
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        BlockState oldState = this.originalState;
        int oldStage = this.infectionStage;

        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.load(tag);
        }

        if (this.level != null && this.level.isClientSide) {
            if (!Objects.equals(this.originalState, oldState) || this.infectionStage != oldStage) {
                requestModelDataUpdate();
                BlockState currentState = getBlockState();
                this.level.sendBlockUpdated(worldPosition, currentState, currentState, Block.UPDATE_CLIENTS);
            }
        }
    }
}