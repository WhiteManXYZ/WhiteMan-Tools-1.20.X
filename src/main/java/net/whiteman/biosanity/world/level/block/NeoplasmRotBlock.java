package net.whiteman.biosanity.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceRegistry;
import net.whiteman.biosanity.world.level.neoplasm.resource.ResourceType;
import net.whiteman.biosanity.world.level.block.entity.NeoplasmRotBE;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.item.ModItems;
import net.whiteman.biosanity.message.ModMessages;
import net.whiteman.biosanity.message.synchronization.SyncNeoplasmRotPacket;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.whiteman.biosanity.world.level.neoplasm.common.NeoplasmConstants.DIRECTIONS;
import static net.whiteman.biosanity.world.level.neoplasm.resource.ResourceRegistry.MAX_RESOURCE_LEVEL;
import static net.whiteman.biosanity.world.level.neoplasm.resource.ResourceRegistry.ResourceTypeEntry;

public class NeoplasmRotBlock extends BaseEntityBlock {
    public static final int MAX_ROT_CLUSTER_SIZE = 10;

    public static final EnumProperty<ResourceType> RESOURCE_TYPE = EnumProperty.create("type", ResourceType.class);
    public static final IntegerProperty RESOURCE_LEVEL = IntegerProperty.create("level", 0, MAX_RESOURCE_LEVEL);
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, MAX_ROT_CLUSTER_SIZE);
    public static final BooleanProperty IS_SOURCE = BooleanProperty.create("is_source");
    public static final BooleanProperty HAS_NUTRIENT = BooleanProperty.create("has_nutrient");

    public static final int MAX_STAGES = 3;
    private static final int MIN_INFECTION_SPEED = 150;
    private static final int MAX_INFECTION_SPEED = 240;
    private static final double NEOPLASM_ROT_DROP_CHANCE = 0.1;
    // Stage 0 -> 0.75 drop chance, etc.
    // Number of values must match MAX_STAGES
    private static final double[] DROP_CHANCES = {0.75, 0.32, 0.05};
    private static final float[] DIG_SPEED_MULTIPLIERS = {1.0f, 1.5f, 2.0f};
    private static final float[] EXPLOSION_RESISTANCE_MULTIPLIERS = {1.0f, 0.7f, 0.4f};
    private static final float[] FLAME_MULTIPLIERS = {1.0f, 0.9f, 0.8f};
    private static final float[] FALL_DAMAGE_MULTIPLIERS = {1.0f, 0.85f, 0.7f};

    public NeoplasmRotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(RESOURCE_TYPE, ResourceType.NONE)
                .setValue(RESOURCE_LEVEL, 0)
                .setValue(DISTANCE, MAX_ROT_CLUSTER_SIZE)
                .setValue(IS_SOURCE, false)
                .setValue(HAS_NUTRIENT, false)
        );
    }

    private void scheduleNextTick(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            int delay = level.random.nextInt(MIN_INFECTION_SPEED, MAX_INFECTION_SPEED);
            level.scheduleTick(pos, this, delay);
        }
    }

    private int getBestNeighborDistance(LevelAccessor level, BlockPos pos, BlockState state) {
        // If current block marked as source, it has distance 1
        if (state.getValue(IS_SOURCE)) return 1;

        int minDistance = MAX_ROT_CLUSTER_SIZE - 1;
        for (Direction dir : DIRECTIONS) {
            BlockState neighbor = level.getBlockState(pos.relative(dir));

            // Vein too has distance 1 (it is also source)
            if (neighbor.getBlock() instanceof NeoplasmVeinBlock) return 1;

            // Other block
            if (neighbor.hasProperty(DISTANCE)) {
                minDistance = Math.min(minDistance, neighbor.getValue(DISTANCE));
            }
        }
        return Math.min(minDistance + 1, MAX_ROT_CLUSTER_SIZE);
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction dir, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        // Calculating best distance based on current neighbors
        int newDist = getBestNeighborDistance(level, pos, state);

        // Update distance if there is the best way
        if (state.getValue(DISTANCE) != newDist) {
            return state.setValue(DISTANCE, newDist);
        }

        return super.updateShape(state, dir, neighborState, level, pos, neighborPos);
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        scheduleNextTick(level, pos);
    }

    @Override
    public void setPlacedBy(Level level, @NotNull BlockPos pos, BlockState state, LivingEntity placer, @NotNull ItemStack stack) {
        // If block was placed by player, set it to source
        level.setBlock(pos, state.setValue(IS_SOURCE, true).setValue(DISTANCE, 1), Block.UPDATE_ALL);
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(HAS_NUTRIENT)) {
            if (level.getBlockEntity(pos) instanceof NeoplasmRotBE blockEntity) {
                blockEntity.tick(level, pos, state, blockEntity);
            }
        }
        // TODO fix diagonal blocks not infecting?
        /// This looks awful when big trees doesn't infect fully.
        /// Maybe making leaves infectable will solve this problem

        // Infect 1 block in random direction
        // only if this block is in devour map and
        // current cluster size allow that.
        if (state.getValue(DISTANCE) >= MAX_ROT_CLUSTER_SIZE) return;

        Direction randomDir = Direction.getRandom(random);
        BlockPos targetPos = pos.relative(randomDir);
        BlockState targetState = level.getBlockState(targetPos);

        ResourceTypeEntry info = ResourceRegistry.getResourceInfo(targetState.getBlock());

        if (info.resourceType().isResource()) {
            // Calculating which distance will be in new block
            // and infect only if distance in our limit
            int targetDist = getBestNeighborDistance(level, targetPos, state);

            if (targetDist < MAX_ROT_CLUSTER_SIZE) {
                level.setBlock(targetPos, ModBlocks.NEOPLASM_ROT_BLOCK.get().defaultBlockState()
                        .setValue(RESOURCE_TYPE, info.resourceType())
                        .setValue(RESOURCE_LEVEL, info.level())
                        .setValue(DISTANCE, targetDist), Block.UPDATE_CLIENTS);

                if (level.getBlockEntity(targetPos) instanceof NeoplasmRotBE be) {
                    be.setOriginalState(targetState);
                    be.setChanged();
                    // Sync a little later for prevent desynchronization
                    var server = level.getServer();
                    server.tell(new TickTask(server.getTickCount(), () -> {
                        if (level.isLoaded(targetPos) && level.getBlockEntity(targetPos) instanceof NeoplasmRotBE actualBe) {
                            if (!actualBe.isRemoved()) {
                                ModMessages.sendToClientsTracking(new SyncNeoplasmRotPacket(targetPos, actualBe.saveWithFullMetadata()), actualBe);
                            }
                        }
                    }));
                }
            }
        }

        scheduleNextTick(level, pos);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new NeoplasmRotBE(pos, state);
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        // Rotting over time by suck resources from block itself
        // and sending to veins
        if (!state.getValue(HAS_NUTRIENT)) {
            if (level.getBlockEntity(pos) instanceof NeoplasmRotBE be) {
                int currentStage = be.getInfectionStage();
                if (currentStage < MAX_STAGES - 1) {
                    be.setInfectionStage(currentStage + 1);
                    /// Maybe make rework this feature?
                    // Sets self containers to self resource types/levels
                    // to send it in the closest way to vein
                    level.setBlock(pos, state.setValue(HAS_NUTRIENT, true), Block.UPDATE_ALL);
                    be.setData(state.getValue(RESOURCE_TYPE), state.getValue(RESOURCE_LEVEL));
                }
            }
        }
        // Random tick for faster infect and
        // if block schedule chain is broken, trying to launch it again
        tick(state, level, pos, random);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RESOURCE_TYPE, RESOURCE_LEVEL, DISTANCE, IS_SOURCE, HAS_NUTRIENT);
    }

    @Override
    public void playerDestroy(@NotNull Level level, @NotNull Player player, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable BlockEntity pBlockEntity, @NotNull ItemStack pTool) {
        if (pBlockEntity instanceof NeoplasmRotBE be && level instanceof ServerLevel) {
            BlockState original = be.getOriginalState();

            if (!player.isCreative()) {
                double currentChance = be.getMultiplier(DROP_CHANCES);
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
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBE be) {
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
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBE be) {
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
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBE be) {
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
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBE be) {
            // Better copy explosion resistance from original
            BlockState original = be.getOriginalState();
            float originalResistance = original.getExplosionResistance(level, pos, explosion);

            return originalResistance * be.getMultiplier(EXPLOSION_RESISTANCE_MULTIPLIERS);
        }
        return super.getExplosionResistance(state, level, pos, explosion);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBE be) {
            // Also on first stage better copy original sounds
            // because infection is not that strong
            if (be.getInfectionStage() < 1 && !be.getOriginalState().isAir()) {
                return be.getOriginalState().getSoundType();
            }
        }
        return super.getSoundType(state, level, pos, entity); // (slime sounds) Maybe make custom sounds?
    }

    @Override
    public void fallOn(@NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float fallDistance) {
        if (level.getBlockEntity(pos) instanceof NeoplasmRotBE be) {
            // We reduce damage depending on the stage of infection
            super.fallOn(level, state, pos, entity, fallDistance * be.getMultiplier(FALL_DAMAGE_MULTIPLIERS));
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.NEOPLASM_ROT_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1, pBlockEntity));
    }
}

