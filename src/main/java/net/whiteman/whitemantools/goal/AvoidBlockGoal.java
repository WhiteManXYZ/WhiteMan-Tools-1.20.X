package net.whiteman.whitemantools.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;

import java.util.function.Predicate;

public class AvoidBlockGoal extends Goal {
    private final Mob mob;
    private final PathNavigation navigation;
    private final Predicate<BlockState> avoidPredicate;
    private final double avoidDistanceX;
    private final double avoidDistanceY;
    private final double nearSpeed;

    private Path path;

    public AvoidBlockGoal(Mob mob, Predicate<BlockState> avoidPredicate, double avoidDistanceX, double avoidDistanceY, double nearSpeed) {
        this.mob = mob;
        this.navigation = mob.getNavigation();
        this.avoidPredicate = avoidPredicate;
        this.avoidDistanceX = avoidDistanceX;
        this.avoidDistanceY = avoidDistanceY;
        this.nearSpeed = nearSpeed;
    }

    @Override
    public boolean canUse() {
        BlockPos mobPos = mob.blockPosition();
        BlockPos nearestBlock = findNearestBlock(mob.level(), mobPos);

        if (nearestBlock == null) return false;

        Vec3 avoidVec = Vec3.atCenterOf(nearestBlock);
        Vec3 direction = mob.position().subtract(avoidVec).normalize().scale(avoidDistanceX);
        Vec3 targetVec = mob.position().add(direction);

        path = navigation.createPath(BlockPos.containing(targetVec), 1);
        return path != null;
    }

    @Override
    public void start() {
        navigation.moveTo(path, nearSpeed);
    }

    @Override
    public boolean canContinueToUse() {
        return !navigation.isDone();
    }

    private BlockPos findNearestBlock(Level level, BlockPos origin) {
        int radiusXZ = (int) Math.ceil(avoidDistanceX);
        int radiusY = (int) Math.ceil(avoidDistanceY);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int dx = -radiusXZ; dx <= radiusXZ; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusXZ; dz <= radiusXZ; dz++) {
                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);

                    BlockState state = level.getBlockState(mutable);
                    if (avoidPredicate.test(state)) {
                        double distSqr = mob.position().distanceToSqr(Vec3.atCenterOf(mutable));
                        if (distSqr <= avoidDistanceX * avoidDistanceX) {
                            return mutable.immutable();
                        }
                    }
                }
            }
        }
        return null;
    }
}
