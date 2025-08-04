package net.whiteman.whitemantools.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.whiteman.whitemantools.WhiteManToolsMod;
import net.whiteman.whitemantools.recipe.PurificationStationRecipe;
import net.whiteman.whitemantools.screen.PurificationStationBlockScreen;

import java.util.List;

@JeiPlugin
public class JEIWhiteManToolsPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(WhiteManToolsMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PurificationStationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<PurificationStationRecipe> purificationRecipes = recipeManager.getAllRecipesFor(PurificationStationRecipe.Type.INSTANCE);
        registration.addRecipes(PurificationStationCategory.PURIFICATION_TYPE, purificationRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        //registration.addRecipeClickArea(PurificationStationBlockScreen.class, 93, 30, 20, 30,
        //        PurificationStationCategory.PURIFICATION_TYPE);
    }
}
