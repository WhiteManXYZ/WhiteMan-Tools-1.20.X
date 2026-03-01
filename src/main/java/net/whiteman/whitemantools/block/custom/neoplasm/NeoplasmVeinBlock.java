package net.whiteman.whitemantools.block.custom.neoplasm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.whiteman.whitemantools.block.ModBlocks;
import net.whiteman.whitemantools.block.entity.NeoplasmDevourBlockEntity;
import org.jetbrains.annotations.NotNull;

public class NeoplasmVeinBlock extends NeoplasmBlock {
    public static final BooleanProperty MATURE = BooleanProperty.create("mature");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final double BRANCHING_CHANCE = 0.01;
    private static final float NEOPLASM_VEIN_FALL_CHANCE = 0.7f;
    private static final float NEOPLASM_VEIN_ORIGINAL_DIRECTION_CHANCE = 0.6f;
    private static final int NEOPLASM_VEIN_REROLL_ATTEMPTS = 6;

    public NeoplasmVeinBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MATURE, false));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, MATURE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void randomTick(BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        // Only a "young" vein (which is not yet mature) can grow
        if (!state.getValue(MATURE)) {
            performGrowth(level, pos, state, random);
        }
    }

    private void performGrowth(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Direction parentDir = state.getValue(FACING);
        Direction growDir = (random.nextFloat() < NEOPLASM_VEIN_ORIGINAL_DIRECTION_CHANCE) ? parentDir : Direction.getRandom(random);

        BlockPos targetPos = pos.relative(growDir);

        if (growDir == Direction.UP && hasNoWallNearby(level, targetPos)) return;
        // We're trying to decrease chance of veins contact
        if (hasNeoplasmNearby(level, targetPos, pos)) {
            for (int i = 0; i < NEOPLASM_VEIN_REROLL_ATTEMPTS; i++) {
                Direction newDir = Direction.getRandom(random);
                BlockPos newTarget = pos.relative(newDir);
                BlockState newState = level.getBlockState(newTarget);

                if (!hasNeoplasmNearby(level, newTarget, pos) && NeoplasmUtils.isReplaceable(newState)) {
                    growDir = newDir;
                    targetPos = newTarget;
                    break;
                }
            }
        }
        if (hasNoWallNearby(level, pos.relative(growDir))) {
            if (random.nextFloat() < NEOPLASM_VEIN_FALL_CHANCE) {
                targetPos = pos.relative(Direction.DOWN);
            }
        }

        BlockState targetState = level.getBlockState(targetPos);

        NeoplasmUtils.ResourceEntry info = NeoplasmUtils.getResourceInfo(targetState.getBlock());
        if (info.type() != NeoplasmResourceType.NONE) {
            // Devour
            level.setBlock(targetPos, ModBlocks.NEOPLASM_DEVOUR_BLOCK.get().defaultBlockState()
                    .setValue(NeoplasmDevourBlock.TYPE, info.type()), 3);

            if (level.getBlockEntity(targetPos) instanceof NeoplasmDevourBlockEntity devourBE) {
                devourBE.setOriginalState(targetState);
            }
            level.setBlock(pos, state.setValue(MATURE, true), 3);
        }
        else if (NeoplasmUtils.isReplaceable(targetState)) {
            // Grow
            level.setBlock(targetPos, this.defaultBlockState().setValue(FACING, growDir), 3);
            // There a chance to branch neoplasm into new vein
            // by just not setting current vein into mature
            if (random.nextDouble() > BRANCHING_CHANCE) {
                level.setBlock(pos, state.setValue(MATURE, true), 3);
            }
        }
    }

    private boolean hasNoWallNearby(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos adj = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(adj);

                    if (!(state.getBlock() instanceof NeoplasmBlock)) {
                        if (state.isCollisionShapeFullBlock(level, adj)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean hasNeoplasmNearby(Level level, BlockPos targetPos, BlockPos currentPos) {
        for (Direction d : Direction.values()) {
            BlockPos neighbor = targetPos.relative(d);
            if (!neighbor.equals(currentPos) && level.getBlockState(neighbor).getBlock() instanceof NeoplasmBlock) {
                return true;
            }
        }
        return false;
    }
}
