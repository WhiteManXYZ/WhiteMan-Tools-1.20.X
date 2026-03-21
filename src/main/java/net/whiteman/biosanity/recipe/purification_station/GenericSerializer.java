package net.whiteman.biosanity.recipe.purification_station;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.whiteman.biosanity.util.block.purification_station.ModifiersUtils.ModifierType;

public class GenericSerializer<T extends AbstractStationRecipe> implements RecipeSerializer<T> {
    private final RecipeFactory<T> factory;

    public GenericSerializer(RecipeFactory<T> factory) {
        this.factory = factory;
    }

    public interface RecipeFactory<T> {
        T create(ResourceLocation id, ItemStack output, Ingredient input, ModifierType modifier, int time, DyeColor color);
    }

    @Override
    public @NotNull T fromJson(@NotNull ResourceLocation id, JsonObject json) {
        Ingredient input = Ingredient.fromJson(json.get("ingredient"));

        String modifierName = GsonHelper.getAsString(json, "modifier").toUpperCase();
        ModifierType modifier = ModifierType.valueOf(modifierName);

        String colorName = GsonHelper.getAsString(json, "color").toUpperCase();
        DyeColor color = DyeColor.valueOf(colorName);

        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));

        int time = GsonHelper.getAsInt(json, "processingTime", 200);

        return factory.create(id, result, input, modifier, time, color);
    }

    @Override
    public @Nullable T fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
        Ingredient input = Ingredient.fromNetwork(buf);
        ModifierType modifier = ModifierType.valueOf(buf.readUtf());
        DyeColor color = DyeColor.valueOf(buf.readUtf());
        ItemStack result = buf.readItem();
        int time = buf.readInt();
        return factory.create(id, result, input, modifier, time, color);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buf, T recipe) {
        recipe.input.toNetwork(buf);
        buf.writeEnum(recipe.modifier);
        buf.writeEnum(recipe.color);
        buf.writeItem(recipe.output);
        buf.writeInt(recipe.time);
    }
}
