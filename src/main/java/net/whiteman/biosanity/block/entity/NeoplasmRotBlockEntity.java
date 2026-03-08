package net.whiteman.biosanity.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.whiteman.biosanity.client.model.ModelProperties;
import org.jetbrains.annotations.NotNull;

public class NeoplasmRotBlockEntity extends BlockEntity {
    public static final int MAX_STAGES = 3;
    // Stage 0 -> 0.75, etc.
    // Number of values must match MAX_STAGES
    private static final double[] DROP_CHANCES = {0.75, 0.35, 0.1};

    private BlockState originalState = Blocks.AIR.defaultBlockState();
    private int overlayStage = 0;

    public NeoplasmRotBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEOPLASM_ROT_BE.get(), pos, state);
    }


    @Override
    public @NotNull ModelData getModelData() {
        return ModelData.builder()
                .with(ModelProperties.ORIGINAL_STATE, originalState)
                .with(ModelProperties.OVERLAY_STAGE, overlayStage)
                .build();
    }

    public double getCurrentDropChance() {
        if (overlayStage >= 0 && overlayStage < DROP_CHANCES.length) {
            return DROP_CHANCES[overlayStage];
        }
        return 0.0;
    }

    public int getOverlayStage() {
        return overlayStage;
    }

    public void setInfectionStage(int stage) {
        this.overlayStage = stage;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void setOriginalState(BlockState state) {
        this.originalState = state;
        setChanged();
    }

    public BlockState getOriginalState() {
        return this.originalState;
    }

    /**
     * A universal getter multiplier based on the current state.
     * @param multipliers Array of results for each stage.
     * @return The multiplier of the current stage (or the last one available in the array).
     */
    public float getMultiplier(float[] multipliers) {
        if (multipliers == null || multipliers.length == 0) return 1.0f;

        int index = Math.min(this.overlayStage, multipliers.length - 1);
        return multipliers[Math.max(0, index)];
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("OriginalBlock", NbtUtils.writeBlockState(originalState));
        tag.putInt("OverlayStage", overlayStage);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OriginalBlock", 10)) { // 10 - compound tag
            HolderGetter<Block> holdergetter = this.level != null ?
                    this.level.holderLookup(Registries.BLOCK) :
                    BuiltInRegistries.BLOCK.asLookup();

            this.originalState = NbtUtils.readBlockState(holdergetter, tag.getCompound("OriginalBlock"));
        }
        this.overlayStage = tag.getInt("OverlayStage");
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
        int oldStage = this.overlayStage;

        super.onDataPacket(net, pkt);

        if (this.level != null && (this.originalState != oldState || this.overlayStage != oldStage)) {
            requestModelDataUpdate();
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}