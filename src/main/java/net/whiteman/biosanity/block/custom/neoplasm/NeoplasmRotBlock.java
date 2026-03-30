package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.block.entity.custom.NeoplasmRotBlockEntity;
import net.whiteman.biosanity.util.block.NeoplasmRegistry;
import net.whiteman.biosanity.item.ModItems;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class NeoplasmRotBlock extends NeoplasmBlock implements EntityBlock {
    public static final EnumProperty<NeoplasmRegistry.ResourceType> RESOURCE_TYPE = EnumProperty.create("type", NeoplasmRegistry.ResourceType.class);
    public static final int MAX_RESOURCE_LEVEL = 7;
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_RESOURCE_LEVEL);

    private static final int MIN_INFECTION_SPEED = 150;
    private static final int MAX_INFECTION_SPEED = 240;
    private static final double NEOPLASM_ROT_DROP_CHANCE = 0.1;
    private static final float[] DIG_SPEED_MULTIPLIERS = {1.0f, 1.5f, 2.0f};
    private static final float[] EXPLOSION_RESISTANCE_MULTIPLIERS = {1.0f, 0.7f, 0.4f};
    private static final float[] FLAME_MULTIPLIERS = {1.0f, 0.9f, 0.8f};
    private static final float[] FALL_DAMAGE_MULTIPLIERS = {1.0f, 0.85f, 0.7f};

    public NeoplasmRotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(RESOURCE_TYPE, NeoplasmRegistry.ResourceType.NONE)
                .setValue(LEVEL, 0)
        );
    }

    private void scheduleNextTick(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            int delay = level.random.nextInt(MIN_INFECTION_SPEED, MAX_INFECTION_SPEED);
            level.scheduleTick(pos, this, delay);
        }
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        scheduleNextTick(level, pos);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        // Infect 1 block in random direction
        // only if this block is in devour map
        Direction[] directions = Direction.values();
        Direction randomDir = directions[random.nextInt(directions.length)];

        BlockPos targetPos = pos.relative(randomDir);
        BlockState targetState = level.getBlockState(targetPos);

        NeoplasmRegistry.ResourceTypeEntry info = NeoplasmRegistry.getResourceInfo(targetState.getBlock());
        if (info.resourceType().isResource()) {
            level.setBlock(targetPos, ModBlocks.NEOPLASM_ROT_BLOCK.get().defaultBlockState()
                    .setValue(NeoplasmRotBlock.RESOURCE_TYPE, info.resourceType())
                    .setValue(NeoplasmRotBlock.LEVEL, info.level()), 3);

            if (level.getBlockEntity(targetPos) instanceof NeoplasmRotBlockEntity be) {
                be.setOriginalState(targetState);
            }
        }

        scheduleNextTick(level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new NeoplasmRotBlockEntity(pos, state);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        // Rotting over time
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBlockEntity be) {
            int currentStage = be.getOverlayStage();
            if (currentStage < NeoplasmRotBlockEntity.MAX_STAGES - 1) {
                be.setInfectionStage(currentStage + 1);
            }
        }
        // Random tick for faster infect and
        // if block schedule chain is broken, trying to launch it again
        tick(state, level, pos, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RESOURCE_TYPE, LEVEL);
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity pBlockEntity, @NotNull ItemStack pTool) {
        if (pBlockEntity instanceof NeoplasmRotBlockEntity be && level instanceof ServerLevel) {
            BlockState original = be.getOriginalState();

            if (!player.isCreative()) {
                double currentChance = be.getCurrentDropChance();
                if (level.random.nextDouble() < currentChance && (!original.isAir() && player.hasCorrectToolForDrops(original))) {
                    // We want the broken block to use a drop table, not the block itself
                    // If player has correct tool for block
                    LootParams.Builder builder = new LootParams.Builder((ServerLevel) level)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                            .withParameter(LootContextParams.BLOCK_STATE, original)
                            .withParameter(LootContextParams.TOOL, player.getMainHandItem())
                            .withParameter(LootContextParams.THIS_ENTITY, player);

                    List<ItemStack> drops = original.getBlock().getDrops(original, builder);

                    for (ItemStack stack : drops) {
                        Block.popResource(level, pos, stack);
                    }

                } else if (level.random.nextDouble() < NEOPLASM_ROT_DROP_CHANCE) {
                    Block.popResource(level, pos, new ItemStack(ModItems.NEOPLASM_ROT.get()));
                }
            }
        }
        super.playerDestroy(level, player, pos, state, pBlockEntity, pTool);
    }

    @Override
    public float getDestroyProgress(@NotNull BlockState state, @NotNull Player player, BlockGetter level, @NotNull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBlockEntity be) {
            // Better copy destroy speed parameters from original
            BlockState original = be.getOriginalState();
            float originalProgress = original.getDestroyProgress(player, level, pos);

            // To prevent breaking our block instantly
            if (originalProgress <= 0 || original.isAir()) {
                return super.getDestroyProgress(state, player, level, pos);
            }

            return originalProgress * be.getMultiplier(DIG_SPEED_MULTIPLIERS);
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBlockEntity be) {
            // Better copy flammability from original
            BlockState original = be.getOriginalState();
            int originalFlammability = original.getFlammability(level, pos, direction);

            int resultFlammability = (int) Math.ceil(originalFlammability * be.getMultiplier(FLAME_MULTIPLIERS));

            // Never allow to be nonflammable
            return Math.max(resultFlammability, 3);
        }
        return super.getFlammability(state, level, pos, direction);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBlockEntity be) {
            // Better copy fire spread speed from original
            BlockState original = be.getOriginalState();
            int originalFireSpreadSpeed = original.getFlammability(level, pos, direction);

            int resultFireSpreadSpeed = (int) Math.ceil(originalFireSpreadSpeed * be.getMultiplier(FLAME_MULTIPLIERS));

            // Always allow spread fire
            return Math.max(resultFireSpreadSpeed, 1);
        }
        return super.getFireSpreadSpeed(state, level, pos, direction);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBlockEntity be) {
            // Better copy explosion resistance from original
            BlockState original = be.getOriginalState();
            float originalResistance = original.getExplosionResistance(level, pos, explosion);

            return originalResistance * be.getMultiplier(EXPLOSION_RESISTANCE_MULTIPLIERS);
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBlockEntity be) {
            // Also on first stage better copy original sounds
            // because infection is not that strong
            if (be.getOverlayStage() < 1 && !be.getOriginalState().isAir()) {
                return be.getOriginalState().getSoundType();
            }
        }
        return super.getSoundType(state, level, pos, entity); // (slime sound) Maybe make custom sound?
    }

    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float fallDistance) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBlockEntity be) {
            // We reduce damage depending on the stage of infection
            super.fallOn(level, state, pos, entity, fallDistance * be.getMultiplier(FALL_DAMAGE_MULTIPLIERS));
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }
}

