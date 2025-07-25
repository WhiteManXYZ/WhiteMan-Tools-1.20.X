package net.whiteman.whitemantools.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.whiteman.whitemantools.WhiteManToolsMod;
import net.whiteman.whitemantools.block.ModBlocks;
import net.whiteman.whitemantools.recipe.PurifierChamberRecipe;
import org.jetbrains.annotations.Nullable;

public class PurifierChamberCategory implements IRecipeCategory<PurifierChamberRecipe> {
    public static final ResourceLocation UID =  new ResourceLocation(WhiteManToolsMod.MOD_ID, "purification");
    public static final ResourceLocation TEXTURE = new ResourceLocation(WhiteManToolsMod.MOD_ID,
            "textures/gui/purification_chamber_block_gui.png");

    public static final RecipeType<PurifierChamberRecipe> PURIFICATION_TYPE =
            new RecipeType<>(UID, PurifierChamberRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public PurifierChamberCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(TEXTURE, 0, 0, 176, 85);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PURIFICATION_CHAMBER_BLOCK.get()));
    }

    @Override
    public RecipeType<PurifierChamberRecipe> getRecipeType() {
        return PURIFICATION_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.whiteman_tools.purification_chamber_block");
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PurifierChamberRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 80, 17).addIngredients(recipe.getIngredients().get(0));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 80, 59).addItemStack(recipe.getResultItem(null));
    }
}
