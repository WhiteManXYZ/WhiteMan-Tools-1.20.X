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
import net.whiteman.whitemantools.recipe.PurifierChamberRecipe;
import net.whiteman.whitemantools.screen.PurificationChamberBlockScreen;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;

import java.util.List;

@JeiPlugin
public class JEIWhiteManToolsPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(WhiteManToolsMod.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PurifierChamberCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();

        List<PurifierChamberRecipe> purificationRecipes = recipeManager.getAllRecipesFor(PurifierChamberRecipe.Type.INSTANCE);
        registration.addRecipes(PurifierChamberCategory.PURIFICATION_TYPE, purificationRecipes);
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addRecipeClickArea(PurificationChamberBlockScreen.class, 93, 30, 20, 30,
                PurifierChamberCategory.PURIFICATION_TYPE);
    }
}
