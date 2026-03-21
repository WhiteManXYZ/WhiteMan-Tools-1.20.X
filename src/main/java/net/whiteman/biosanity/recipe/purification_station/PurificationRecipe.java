package net.whiteman.biosanity.recipe.purification_station;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.whiteman.biosanity.recipe.ModRecipes;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.util.block.purification_station.ModifiersUtils.ModifierType;

public class PurificationRecipe extends AbstractStationRecipe {
    public PurificationRecipe(ResourceLocation id, ItemStack output, Ingredient input, ModifierType modifier, int time, DyeColor color) {
        super(id, output, input, modifier, time, color);
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return ModRecipes.PURIFICATION_SERIALIZER.get(); }
    @Override
    public @NotNull RecipeType<?> getType() { return ModRecipes.PURIFICATION_TYPE.get(); }
}
