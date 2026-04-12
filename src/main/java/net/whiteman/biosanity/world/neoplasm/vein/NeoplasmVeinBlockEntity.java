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

import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConfig.TICKS_TO_SEND_IMPULSE;
import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConfig.TICKS_TO_TRANSFER_NUTRIENT;
import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConstants.DIRECTIONS;
import static net.whiteman.biosanity.world.neoplasm.vein.NeoplasmVeinBlock.*;

public class NeoplasmVeinBlockEntity extends BlockEntity {
    public Direction growthDirection = Direction.DOWN;
    public Direction parentDirection = Direction.DOWN;
    public Direction childDirection = Direction.DOWN;

    private ResourceType heldResourceType = ResourceType.NONE;
    private int heldResourceLevel = 0;
    private int transferCooldown = 0;

    private ImpulsePacket activeImpulse = null;
    private int impulseCooldown = 0;

    public NeoplasmVeinBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.NEOPLASM_VEIN_BE.get(), pPos, pBlockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state, NeoplasmVeinBlockEntity be) {
        if (state.getValue(HAS_NUTRIENT) || be.heldResourceType != ResourceType.NONE) {
            // Transfer countdown
            if (be.transferCooldown > 0) {
                be.transferCooldown--;
                return;
            }

            be.transferNutrient(level, pos, state);
        }

        if (be.activeImpulse != null) {
            // Send countdown
            if (be.impulseCooldown > 0) {
                be.impulseCooldown--;
                return;
            }

            be.sendImpulse(level, pos, state);
        }
    }

    /** Transfers nutrients to core by chain method */
    private void transferNutrient(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        Direction toParent = parentDirection;
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

    /** Sends impulse by chain method to the vein end */
    private void sendImpulse(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        // If we are the end of the vein, we receive the impulse
        if (!state.getValue(MATURE)) {
            acceptImpulse(activeImpulse);
            return;
        }

        Direction toChild = childDirection;
        BlockPos targetPos = pos.relative(toChild);
        BlockState targetState = level.getBlockState(targetPos);
        BlockEntity targetEntity = level.getBlockEntity(targetPos);

        if (targetState.getBlock() instanceof NeoplasmVeinBlock block) {
            if (targetEntity instanceof NeoplasmVeinBlockEntity blockEntity) {

                blockEntity.setImpulsePacket(activeImpulse);
                blockEntity.setImpulseSendingCooldown(TICKS_TO_SEND_IMPULSE);
            }

            // TEST PARTICLE
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.BUBBLE_POP,
                        targetPos.getX() + 0.5,
                        targetPos.getY() + 0.7,
                        targetPos.getZ() + 0.5,
                        10,
                        0.4, 0.4, 0.4,
                        0.05
                );
            }
        }
        else {
            if (level.getBlockEntity(activeImpulse.sourceCore()) instanceof NeoplasmCoreBlockEntity blockEntity) {
                blockEntity.receiveFailedImpulse(activeImpulse);
            } else {
                System.out.printf("Impulse %s lost in position: %s and don't returned result to core!\n", activeImpulse, targetPos);
            }
        }

        clearImpulse();
    }

    /** Accepts impulse and do some action depending on {@link ImpulseType} */
    private void acceptImpulse(ImpulsePacket packet) {
        if (level == null || level.isClientSide || packet == null) return;
        BlockState state = level.getBlockState(worldPosition);

        if (state.getBlock() instanceof NeoplasmVeinBlock veinBlock) {
            switch (packet.type()) {
                case GROW -> {
                    if (veinBlock.canSpread(level, worldPosition, packet.hiveLevel())) {
                        veinBlock.performGrowth(level, worldPosition, state, packet);
                    }
                }
                case SCAN -> System.out.println("SCAN");
            }
        }
        if (level.getBlockEntity(packet.sourceCore()) instanceof NeoplasmCoreBlockEntity blockEntity) {
            blockEntity.receiveImpulseSuccess(packet);
        }

        clearImpulse();
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

    public void setImpulsePacket(ImpulsePacket packet) {
        this.activeImpulse = packet;
        this.setChanged();
    }

    public void clearImpulse() {
        this.activeImpulse = null;
        this.setChanged();
    }

    public void setImpulseSendingCooldown(int ticks) {
        this.impulseCooldown = ticks;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.putString("held_resource_type", heldResourceType.name());
        tag.putInt("held_resourceLevel", heldResourceLevel);

        if (activeImpulse != null) {
            tag.put("active_impulse", activeImpulse.toNBT());
            tag.putInt("impulse_cooldown", impulseCooldown);
        }

        tag.putInt("growth_direction", this.growthDirection.get3DDataValue());
        tag.putInt("parent_direction", this.parentDirection.get3DDataValue());
        tag.putInt("child_direction", this.childDirection.get3DDataValue());
        super.saveAdditional(tag);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.heldResourceType = ResourceType.valueOf(tag.getString("held_resource_type"));
        this.heldResourceLevel = tag.getInt("held_resourceLevel");

        if (tag.contains("active_impulse")) {
            this.activeImpulse = ImpulsePacket.fromNBT(tag.getCompound("active_impulse"));
            this.impulseCooldown = tag.getInt("impulse_cooldown");
        }

        this.growthDirection = Direction.from3DDataValue(tag.getInt("growth_direction"));
        this.parentDirection = Direction.from3DDataValue(tag.getInt("parent_direction"));
        this.childDirection = Direction.from3DDataValue(tag.getInt("child_direction"));
    }
}
