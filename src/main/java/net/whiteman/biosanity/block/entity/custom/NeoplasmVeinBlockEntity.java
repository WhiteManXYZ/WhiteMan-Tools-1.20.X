package net.whiteman.biosanity.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.block.custom.neoplasm.NeoplasmVeinBlock;
import net.whiteman.biosanity.block.entity.ModBlockEntities;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.block.custom.neoplasm.NeoplasmVeinBlock.HAS_NUTRIENT;
import static net.whiteman.biosanity.block.custom.neoplasm.NeoplasmVeinBlock.PARENT_DIRECTION;
import static net.whiteman.biosanity.util.block.NeoplasmUtils.ResourceRegistry.*;

public class NeoplasmVeinBlockEntity extends BlockEntity {
    public static final int TICKS_TO_TRANSFER_NUTRIENT = 5;

    private ResourceType type = ResourceType.NONE;
    private int level = 0;
    private int transferCooldown = 0;

    public NeoplasmVeinBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_VEIN_BE.get(), pPos, pBlockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmVeinBlockEntity be) {
        if (!state.getValue(HAS_NUTRIENT)) return;

        // Transfer countdown
        if (be.transferCooldown > 0) {
            be.transferCooldown--;
            return;
        }

        be.transferNutrient(level, pos, state);
    }

    // Nutrient transfer
    // Transfers nutrients to core using "chain" method
    private void transferNutrient(Level level, BlockPos pos, BlockState state) {
        Direction toParent = state.getValue(PARENT_DIRECTION);
        BlockPos targetPos = pos.relative(toParent);
        BlockState targetState = level.getBlockState(targetPos);
        BlockEntity targetEntity = level.getBlockEntity(targetPos);

        // Transfer logic
        if (targetState.getBlock() instanceof NeoplasmVeinBlock block) {
            // Don't move resources if they have one
            if (targetState.getValue(HAS_NUTRIENT)) {
                level.scheduleTick(pos, state.getBlock(), TICKS_TO_TRANSFER_NUTRIENT);
                return;
            }

            // Target vein
            level.setBlock(targetPos, targetState.setValue(HAS_NUTRIENT, true), 3);

            if (targetEntity instanceof NeoplasmVeinBlockEntity blockEntity) {
                blockEntity.setData(this.type, this.level);
                blockEntity.transferCooldown = TICKS_TO_TRANSFER_NUTRIENT;
            }

            // Current vein
            level.setBlock(pos, state.setValue(HAS_NUTRIENT, false), 3);
            this.setData(ResourceType.NONE, 0);


            // TEST PARTICLE
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.7,
                        targetPos.getZ() + 0.5,
                        10,
                        0.4, 0.4, 0.4,
                        0.05
                );
            }
        }
        // Put to core logic
        else if (targetEntity instanceof NeoplasmCoreBlockEntity core) {
            core.decomposeResource(this.type, this.level);
            level.setBlock(pos, state.setValue(HAS_NUTRIENT, false), 3);
            this.setData(ResourceType.NONE, 0);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString("resourceType", type.name());
        tag.putInt("resourceLevel", level);
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.type = ResourceType.valueOf(tag.getString("resourceType"));
        this.level = tag.getInt("resourceLevel");
    }

    public void setData(ResourceType type, int level) {
        this.type = type;
        this.level = level;
        this.setChanged();
    }

    public ResourceType getResourceType() { return type; }
    public int getResourceLevel() { return level; }
}
