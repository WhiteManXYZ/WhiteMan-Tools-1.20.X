package net.whiteman.biosanity.world.neoplasm.common;

import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.Collection;

public class NeoplasmConstants {
    public static final Direction[] DIRECTIONS = Direction.values();
    public static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    /** Shuffles directions array,
     * useful when needed to randomize each direction checking
     * @return Shuffled collection of directions */
    public static Collection<Direction> getShuffledDirections(RandomSource random) {
        return Direction.allShuffled(random);
    }
}