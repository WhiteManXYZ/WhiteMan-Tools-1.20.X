package net.whiteman.biosanity.world.level.neoplasm.resource;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ResourceType implements StringRepresentable {
    NONE("none"),
    BIOMASS("biomass"),
    MINERAL("mineral"),
    ENERGY("energy");

    private final String name;

    ResourceType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public boolean isResource() {
        return this != NONE;
    }
}