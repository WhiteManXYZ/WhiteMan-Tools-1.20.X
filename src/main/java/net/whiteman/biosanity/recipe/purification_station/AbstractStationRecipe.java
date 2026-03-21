package net.whiteman.biosanity.recipe.purification_station;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.util.block.purification_station.ModifiersUtils.ModifierType;

public abstract class AbstractStationRecipe implements Recipe<SimpleContainer> {
    protected final ResourceLocation id;
    protected final ItemStack output;
    protected final Ingredient input;
    protected final ModifierType modifier;
    protected final int time;
    protected final DyeColor color;

    public AbstractStationRecipe(ResourceLocation id, ItemStack output, Ingredient input, ModifierType modifier, int time, DyeColor color) {
        this.id = id;
        this.output = output;
        this.input = input;
        this.modifier = modifier;
        this.time = time;
        this.color = color;
    }

    @Override
    public boolean matches(SimpleContainer container, @NotNull Level level) {
        return this.input.test(container.getItem(0));
    }

    public ModifierType getModifier() { return modifier; }
    public int getTime() { return time; }
    public Ingredient getInput() { return input; }
    public DyeColor getColor() { return color; }
    @Override public @NotNull ResourceLocation getId() { return id; }
    @Override public @NotNull ItemStack getResultItem(@NotNull RegistryAccess access) { return output.copy(); }
    @Override public boolean canCraftInDimensions(int w, int h) { return true; }
    @Override public @NotNull ItemStack assemble(@NotNull SimpleContainer cont, @NotNull RegistryAccess acc) { return output.copy(); }
}