package net.whiteman.whitemantools.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.function.Predicate;

public class AvoidBlockGoal extends Goal {
    private final Mob mob;
    private final PathNavigation navigation;
    private final Predicate<BlockState> avoidPredicate;
    private final double avoidDistanceX;
    private final double avoidDistanceY;
    private final double nearSpeed;
    private Path path;
    private BlockPos avoidBlockPos;

    public AvoidBlockGoal(Mob mob, Predicate<BlockState> avoidPredicate, double avoidDistanceX, double avoidDistanceY, double nearSpeed) {
        this.mob = mob;
        this.navigation = mob.getNavigation();
        this.avoidPredicate = avoidPredicate;
        this.avoidDistanceX = avoidDistanceX;
        this.avoidDistanceY = avoidDistanceY;
        this.nearSpeed = nearSpeed;

        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos mobPos = mob.blockPosition();
        avoidBlockPos = findNearestBlock(mob.level(), mobPos);
        if (avoidBlockPos == null) return false;

        Vec3 avoidVec = Vec3.atCenterOf(avoidBlockPos);
        Vec3 direction = mob.position().subtract(avoidVec);

        double dx = direction.x;
        double dz = direction.z;
        double horizontalDistSqr = dx * dx + dz * dz;

        if (horizontalDistSqr < 0.5) {
            direction = getRandomHorizontalDirection();
        } else {
            direction = direction.normalize();

            // add angular deviation
            double angleOffset = mob.getRandom().nextDouble() * 60.0 - 30.0; // -30 to +30 degrees
            direction = rotateVectorY(direction, angleOffset);
        }

        double minDistance = 1.0;
        direction = direction.scale(Math.max(avoidDistanceX, minDistance));

        Vec3 targetVec = mob.position().add(direction);
        path = navigation.createPath(BlockPos.containing(targetVec), 1);
        return path != null;
    }

    @Override
    public void start() {
        if (path != null) {
            navigation.moveTo(path, nearSpeed);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !navigation.isDone();
    }

    @Override
    public void tick() {
        if (avoidBlockPos != null) {
            Vec3 mobPos = mob.position();
            Vec3 avoidVec = Vec3.atCenterOf(avoidBlockPos);

            double distSqr = mobPos.distanceToSqr(avoidVec);
            double fireRadius = avoidDistanceX * 0.6;
            double soundRadius = avoidDistanceX * 1.2;

            RandomSource random = mob.getRandom();

            if (distSqr < fireRadius * fireRadius && random.nextDouble() < 0.2) {
                mob.setSecondsOnFire(1);
            }

            if (distSqr < soundRadius * soundRadius && random.nextDouble() < 0.05) {
                mob.playSound(SoundEvents.FIRE_EXTINGUISH, 0.5F, 2.0F);
                mob.setSecondsOnFire(3);
            }
        }
    }

    @Override
    public void stop() {
        avoidBlockPos = null;
    }

    @Override
    public @NotNull EnumSet<Flag> getFlags() {
        return EnumSet.of(Flag.MOVE);
    }

    private BlockPos findNearestBlock(Level level, BlockPos origin) {
        int radiusXZ = (int) Math.ceil(avoidDistanceX);
        int radiusY = (int) Math.ceil(avoidDistanceY);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        double nearestDistSqr = Double.MAX_VALUE;
        BlockPos nearest = null;

        for (int dx = -radiusXZ; dx <= radiusXZ; dx++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
                for (int dz = -radiusXZ; dz <= radiusXZ; dz++) {
                    mutable.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = level.getBlockState(mutable);

                    if (avoidPredicate.test(state)) {
                        double distSqr = mob.position().distanceToSqr(Vec3.atCenterOf(mutable));
                        if (distSqr < nearestDistSqr) {
                            nearestDistSqr = distSqr;
                            nearest = mutable.immutable();
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private Vec3 getRandomHorizontalDirection() {
        RandomSource random = mob.getRandom();
        double angle = random.nextDouble() * 2 * Math.PI;
        return new Vec3(Math.cos(angle), 0, Math.sin(angle));
    }

    private Vec3 rotateVectorY(Vec3 vec, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = vec.x * cos - vec.z * sin;
        double z = vec.x * sin + vec.z * cos;
        return new Vec3(x, vec.y, z);
    }
}
