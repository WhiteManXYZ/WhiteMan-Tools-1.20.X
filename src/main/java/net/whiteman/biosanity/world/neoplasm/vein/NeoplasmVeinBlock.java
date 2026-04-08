package net.whiteman.biosanity.world.neoplasm.vein;

import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.world.neoplasm.common.node.INeoplasmNode;
import net.whiteman.biosanity.world.level.block.entity.ModBlockEntities;
import net.whiteman.biosanity.world.neoplasm.core.hivemind.HivemindLevel;
import net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry;
import net.whiteman.biosanity.world.neoplasm.rot.NeoplasmRotBlockEntity;
import net.whiteman.biosanity.message.ModMessages;
import net.whiteman.biosanity.message.synchronization.SyncNeoplasmRotPacket;
import net.whiteman.biosanity.world.neoplasm.rot.NeoplasmRotBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.whiteman.biosanity.world.neoplasm.common.NeoplasmConstants.DIRECTIONS;
import static net.whiteman.biosanity.world.neoplasm.resource.ResourceRegistry.ResourceTypeEntry;

public class NeoplasmVeinBlock extends BaseEntityBlock implements INeoplasmNode {
    // States
    public static final BooleanProperty MATURE = BooleanProperty.create("mature");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final DirectionProperty PARENT_DIRECTION = DirectionProperty.create("parent_direction", DIRECTIONS);
    public static final BooleanProperty HAS_NUTRIENT = BooleanProperty.create("has_nutrient");
    // Base params
    private static final double BRANCHING_CHANCE = 0.02;
    private static final double FALL_CHANCE = 0.75;
    private static final double ORIGINAL_DIRECTION_CHANCE = 0.45;
    private static final int REROLL_ATTEMPTS = 10;
    private static final double MATURE_CHANCE = 0.004;
    // Tick rate params
    private static final int MIN_TICKS_TO_SPREAD = 140;
    private static final int MAX_TICKS_TO_SPREAD = 400;
    private static final double RAIN_WEATHER_SPREAD_MODIFIER = 0.9;
    private static final double NIGHT_SPREAD_MODIFIER = 0.8;

    public NeoplasmVeinBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(MATURE, false)
                .setValue(HAS_NUTRIENT, false));
    }

    private void scheduleNextTick(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            double delay = level.random.nextInt(MIN_TICKS_TO_SPREAD, MAX_TICKS_TO_SPREAD);
            // We apply spread speed modifiers for variable gameplay
            // and don't allow delay to be lower than 20 ticks
            level.scheduleTick(pos, this, Math.max(spreadSpeedModifiers(level, delay), 20));
        }
    }

    /// WIP
    /// Maybe make smart resource searching?
    /// Don't allow to grow forward while falling down
    private void performGrowth(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        // There a chance to just grow up and end vein chain
        if (random.nextDouble() < MATURE_CHANCE) {
            level.setBlock(pos, state.setValue(MATURE, true), Block.UPDATE_ALL);
            return;
        }
        // Original dir contains original "root vein" direction
        net.minecraft.core.Direction originalDir = state.getValue(FACING);
        net.minecraft.core.Direction growDir = (random.nextDouble() < ORIGINAL_DIRECTION_CHANCE) ? originalDir : net.minecraft.core.Direction.getRandom(random);

        // Our special conditions to better vein spread
        if (!conditions(level, pos, originalDir, growDir)) { return; }

        // We're trying to decrease chance of veins contact
        // or increase spread chance, if there is unreplaceable block
        // TODO make hivemind dependency
        if (hasNeoplasmNearby(level, pos.relative(growDir), pos) || !ResourceRegistry.isReplaceable(level.getBlockState(pos.relative(growDir)), HivemindLevel.T1)) {
            for (int i = 0; i < REROLL_ATTEMPTS; i++) {
                net.minecraft.core.Direction newDir = net.minecraft.core.Direction.getRandom(random);

                if (!hasNeoplasmNearby(level, pos.relative(newDir), pos)) {
                    growDir = newDir;
                    break;
                }
            }
        }

        // Increase vein chance to grow downwards when there is no wall
        if (hasNoWallNearby(level, pos.relative(growDir))) {
            if (random.nextDouble() < FALL_CHANCE) {
                growDir = net.minecraft.core.Direction.DOWN;
            }
        }

        // Deciding what vein supposed to do:
        // Spread or Absorb resource
        ResourceResult nearbyResource = findResourceNearby(level, pos);
        // Infection resource blocks is more important than spread
        if (nearbyResource != null) {
            absorbResources(level, pos.relative(nearbyResource.direction), nearbyResource.info, nearbyResource.state);
        }
        else {
            BlockState targetState = level.getBlockState(pos.relative(growDir));
            // TODO make hivemind dependency
            if (ResourceRegistry.isReplaceable(targetState, HivemindLevel.T1)) {
                grow(level, pos, state, random, growDir, originalDir);
            }
        }
    }

    // Absorb
    // Creates a "patient-zero" absorbed resource
    // that continue spread rot blocks by himself
    private void absorbResources(ServerLevel level, BlockPos targetPos, ResourceTypeEntry info, BlockState targetState) {
        level.setBlock(targetPos, ModBlocks.NEOPLASM_ROT_BLOCK.get().defaultBlockState()
                .setValue(NeoplasmRotBlock.RESOURCE_TYPE, info.resourceType())
                .setValue(NeoplasmRotBlock.LEVEL, info.level()), Block.UPDATE_CLIENTS);

        if (level.getBlockEntity(targetPos) instanceof NeoplasmRotBlockEntity devourBE) {
            devourBE.setOriginalState(targetState);
            devourBE.setChanged();
            // Sync a little later for prevent desynchronization
            var server = level.getServer();
            server.tell(new TickTask(server.getTickCount(), () -> {
                if (level.isLoaded(targetPos) && level.getBlockEntity(targetPos) instanceof NeoplasmRotBlockEntity actualBe) {
                    if (!actualBe.isRemoved()) {
                        ModMessages.sendToClientsTracking(new SyncNeoplasmRotPacket(targetPos, actualBe.saveWithFullMetadata()), actualBe);
                    }
                }
            }));
        }
    }

    // Grow
    // Has a chance to "split" in different directions
    // by just not setting current vein into mature
    // and changing original grow direction (to spread nor in 1 dir.)
    private void grow(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, net.minecraft.core.Direction growDir, net.minecraft.core.Direction originalDir) {
        BlockPos targetPos = pos.relative(growDir);
        if (random.nextDouble() > BRANCHING_CHANCE) {
            // Target block
            level.setBlock(targetPos, this.defaultBlockState()
                    .setValue(PARENT_DIRECTION, growDir.getOpposite())
                    .setValue(FACING, originalDir), Block.UPDATE_ALL);
            scheduleNextTick(level, targetPos);
            // Current block
            level.setBlock(pos, state.setValue(MATURE, true), Block.UPDATE_ALL);
        } else if (!targetPos.relative(originalDir.getOpposite()).equals(pos)) {
            net.minecraft.core.Direction nextDir = calculateOriginalDirection(pos, random, targetPos, originalDir);
            if (nextDir == null) return;
            // Only target block
            level.setBlock(targetPos, this.defaultBlockState()
                    .setValue(PARENT_DIRECTION, growDir.getOpposite())
                    .setValue(FACING, nextDir), Block.UPDATE_ALL);
            scheduleNextTick(level, targetPos);
        }
    }

    private static net.minecraft.core.Direction calculateOriginalDirection(BlockPos pos, RandomSource random, BlockPos targetPos, net.minecraft.core.Direction originalDir) {
        // Branch direction calculator
        net.minecraft.core.Direction branchDir = null;
        net.minecraft.core.Direction nextDir;
        // Calculating relative coordinates
        for (net.minecraft.core.Direction dir : DIRECTIONS) {
            if (targetPos.relative(dir.getOpposite()).equals(pos)) {
                branchDir = dir;
                break;
            }
        }
        if (branchDir == null) return null;

        // Don't allow to set facing vertical
        // or opposite from original direction
        if (branchDir.getAxis().isVertical()) {
            do {
                branchDir = net.minecraft.core.Direction.getRandom(random);
            } while (branchDir.getAxis().isVertical() || branchDir == originalDir.getOpposite());
        }
        nextDir = branchDir;
        return nextDir;
    }

    private static boolean conditions(Level level, BlockPos pos, net.minecraft.core.Direction originalDir, net.minecraft.core.Direction growDir) {
        // Don't allow to grow backwards
        if (growDir == originalDir.getOpposite()) return false;
        // Don't allow to climb up without wall
        if (growDir == net.minecraft.core.Direction.UP && hasNoWallNearby(level, pos.relative(growDir))) return false;

        return true;
    }

    private static int spreadSpeedModifiers(Level level, double delay) {
        if (level.isRaining()) delay *= RAIN_WEATHER_SPREAD_MODIFIER;
        if (level.isNight()) delay *= NIGHT_SPREAD_MODIFIER;
        return (int) Math.ceil(delay);
    }

    private static boolean hasNoWallNearby(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos adj = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(adj);

                    if (!(state.getBlock() instanceof INeoplasmNode)) {
                        if (state.isCollisionShapeFullBlock(level, adj)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean hasNeoplasmNearby(Level level, BlockPos targetPos, BlockPos currentPos) {
        for (net.minecraft.core.Direction d : DIRECTIONS) {
            BlockPos neighbor = targetPos.relative(d);
            if (!neighbor.equals(currentPos) && level.getBlockState(neighbor).getBlock() instanceof INeoplasmNode) {
                return true;
            }
        }
        return false;
    }

    private record ResourceResult(net.minecraft.core.Direction direction, ResourceTypeEntry info, BlockState state) {}

    private static ResourceResult findResourceNearby(Level level, BlockPos pos) {
        for (net.minecraft.core.Direction d : DIRECTIONS) {
            BlockPos checkPos = pos.relative(d);
            BlockState state = level.getBlockState(checkPos);
            ResourceTypeEntry info = ResourceRegistry.getResourceInfo(state.getBlock());

            if (info.resourceType().isResource()) {
                return new ResourceResult(d, info, state);
            }
        }
        return null;
    }

    private static boolean hasNonMatureNearby(Level level, BlockPos pos) {
        for (net.minecraft.core.Direction d : DIRECTIONS) {
            BlockPos checkPos = pos.relative(d);
            BlockState state = level.getBlockState(checkPos);

            if (state.hasProperty(MATURE) && !state.getValue(MATURE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new NeoplasmVeinBlockEntity(pos, state);
    }

    @Override
    public void tick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(HAS_NUTRIENT)) {
            if (level.getBlockEntity(pos) instanceof NeoplasmVeinBlockEntity blockEntity) {
                blockEntity.tick(level, pos, state, blockEntity);
            }
        }
        // Only a "young" vein (which is not yet mature) can grow
        if (!state.getValue(MATURE)) {
            performGrowth(level, pos, state, random);
        }
        scheduleNextTick(level, pos);
    }

    @Override
    public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (!state.getValue(MATURE)) {
            // Random tick for faster spread and
            // if block schedule chain is broken, trying to launch it again
            performGrowth(level, pos, state, random);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            return;
        }
        // If the living end of our vein is destroyed, we make the previous block young again
        // (and all neighbors too)
        for (net.minecraft.core.Direction dir : DIRECTIONS) {
            BlockPos neighborPos = pos.relative(dir);
            BlockState neighborState = level.getBlockState(neighborPos);

            if ((state.hasProperty(MATURE) && !state.getValue(MATURE)) || hasNonMatureNearby(level, pos)) {
                if (neighborState.hasProperty(MATURE) && neighborState.getValue(MATURE)) {
                    level.setBlock(neighborPos, neighborState.setValue(MATURE, false), Block.UPDATE_ALL);

                    // Test particle
                    level.levelEvent(2001, neighborPos, Block.getId(neighborState));
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MATURE, PARENT_DIRECTION, HAS_NUTRIENT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        scheduleNextTick(level, pos);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, @NotNull BlockState pState, @NotNull BlockEntityType<T> pBlockEntityType) {
        if (pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.NEOPLASM_VEIN_BE.get(),
                (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1, pBlockEntity));
    }
}
