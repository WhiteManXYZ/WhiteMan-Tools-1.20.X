package net.whiteman.biosanity.world.neoplasm.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBlockEntity;
import net.whiteman.biosanity.world.neoplasm.resource.ResourceType;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConstants.DIRECTIONS;
import static net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock.HAS_NUTRIENT;
import static net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock.PARENT_DIRECTION;

public class NeoplasmVeinBlockEntity extends BlockEntity {
    public static final int TICKS_TO_TRANSFER_NUTRIENT = 5;

    private ResourceType heldResourceType = ResourceType.NONE;
    private int heldResourceLevel = 0;
    private int transferCooldown = 0;

    public NeoplasmVeinBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_VEIN_BE.get(), pPos, pBlockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmVeinBlockEntity be) {
        if (!state.getValue(HAS_NUTRIENT) || be.heldResourceType == ResourceType.NONE) return;

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
        if (level.isClientSide) return;

        Direction toParent = state.getValue(PARENT_DIRECTION);
        BlockPos targetPos = pos.relative(toParent);
        BlockState targetState = level.getBlockState(targetPos);
        BlockEntity targetEntity = level.getBlockEntity(targetPos);

        // First priority is transfer to the "parent vein"
        // because core cant just suck resource from side vein
        // only from vein which is the end of the segment
        if (targetState.getBlock() instanceof NeoplasmVeinBlock block) {
            // If vein has resource, do nothing and request update later
            if (targetState.getValue(HAS_NUTRIENT)) {
                level.scheduleTick(pos, state.getBlock(), TICKS_TO_TRANSFER_NUTRIENT);
                return;
            }

            // Target vein
            level.setBlock(targetPos, targetState.setValue(HAS_NUTRIENT, true), Block.UPDATE_ALL);

            if (targetEntity instanceof NeoplasmVeinBlockEntity blockEntity) {
                // Target vein
                blockEntity.setData(this.heldResourceType, this.heldResourceLevel);
                blockEntity.transferCooldown = TICKS_TO_TRANSFER_NUTRIENT;
                // Current vein
                level.setBlock(pos, state.setValue(HAS_NUTRIENT, false), Block.UPDATE_ALL);
                this.clearResource();
            }

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
        // Second priority is: deliver resources to the core
        else for (Direction dir : DIRECTIONS) {
            BlockEntity targetCoreEntity = level.getBlockEntity(pos.relative(dir));

            if (targetCoreEntity instanceof NeoplasmCoreBlockEntity core) {
                // Target core
                boolean isDecomposed = core.decomposeResource(this.heldResourceType, this.heldResourceLevel);
                // Current vein
                if (!isDecomposed) return;

                level.setBlock(pos, state.setValue(HAS_NUTRIENT, false), Block.UPDATE_ALL);
                this.clearResource();


                // TEST PARTICLE
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.WAX_ON,
                            targetCoreEntity.getBlockPos().getX() + 0.5,
                            targetCoreEntity.getBlockPos().getY() + 0.9,
                            targetCoreEntity.getBlockPos().getZ() + 0.5,
                            20,
                            0.4, 0.4, 0.4,
                            0.05
                    );
                }
                break;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString("heldResourceType", heldResourceType.name());
        tag.putInt("heldResourceLevel", heldResourceLevel);
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.heldResourceType = ResourceType.valueOf(tag.getString("heldResourceType"));
        this.heldResourceLevel = tag.getInt("heldResourceLevel");
    }

    public void setData(ResourceType type, int level) {
        this.heldResourceType = type;
        this.heldResourceLevel = level;
        this.setChanged();
    }

    private void clearResource() {
        this.heldResourceType = ResourceType.NONE;
        this.heldResourceLevel = 0;
        this.setChanged();
    }

    public void setNutrientTransferCooldown(int ticks) {
        this.transferCooldown = ticks;
    }
}
