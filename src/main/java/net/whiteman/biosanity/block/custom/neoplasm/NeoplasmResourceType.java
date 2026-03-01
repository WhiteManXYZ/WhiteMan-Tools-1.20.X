package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum NeoplasmResourceType implements StringRepresentable {
    NONE("none"),
    BIOMASS("biomass"),
    MINERAL("mineral"),
    ENERGY("energy");

    private final String name;

    NeoplasmResourceType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}