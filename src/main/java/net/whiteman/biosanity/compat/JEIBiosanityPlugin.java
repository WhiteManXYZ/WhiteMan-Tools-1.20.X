package net.whiteman.biosanity.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.compat.purification_station_categories.PaintingCategory;
import net.whiteman.biosanity.compat.purification_station_categories.PurificationCategory;
import net.whiteman.biosanity.recipe.ModRecipes;
import net.whiteman.biosanity.screen.PurificationStationBlockScreen;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIBiosanityPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return new ResourceLocation(BiosanityMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();

        registration.addRecipeCategories(new PurificationCategory(helper));
        registration.addRecipeCategories(new PaintingCategory(helper));
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) return;
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        registration.addRecipes(PurificationCategory.PURIFICATION_TYPE,
                recipeManager.getAllRecipesFor(ModRecipes.PURIFICATION_TYPE.get()));

        // TODO(whiteman) make filter for recipes (yellow wool to yellow wool fix)
        // TODO(whiteman) add usage screens for dyes and sand dust
        registration.addRecipes(PaintingCategory.PAINTING_TYPE,
                recipeManager.getAllRecipesFor(ModRecipes.PAINTING_TYPE.get()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.PURIFICATION_STATION_BLOCK.get()),
                PurificationCategory.PURIFICATION_TYPE
        );
        registration.addRecipeCatalyst(
                new ItemStack(ModBlocks.PURIFICATION_STATION_BLOCK.get()),
                PaintingCategory.PAINTING_TYPE
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PurificationStationBlockScreen.class, 92, 37, 25, 21,
                PurificationCategory.PURIFICATION_TYPE,
                PaintingCategory.PAINTING_TYPE
        );
    }
}
