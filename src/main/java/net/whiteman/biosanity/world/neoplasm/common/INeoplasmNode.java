package net.whiteman.biosanity.world.neoplasm.common;

public interface INeoplasmNode {
    default boolean isCore() {
        return false;
    }

    default boolean isVein() {
        return false;
    }
}
