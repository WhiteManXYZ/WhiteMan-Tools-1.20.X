package net.whiteman.biosanity.compat.purification_station_categories;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.world.item.crafting.PurificationRecipe;
import org.jetbrains.annotations.NotNull;

public class PurificationCategory extends AbstractJettingCategory<PurificationRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(BiosanityMod.MOD_ID, "purification");

    public static final RecipeType<PurificationRecipe> PURIFICATION_TYPE =
            new RecipeType<>(UID, PurificationRecipe.class);

    public PurificationCategory(IGuiHelper helper) {
        super(helper, 205, 51);
    }

    public void draw(@NotNull PurificationRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        bubbles.draw(guiGraphics, 6, 35);
        pressureIcon.draw(guiGraphics, 104, 0);
        fuelIcon.draw(guiGraphics, 2, 48);
        sandModifierIcon.draw(guiGraphics, 22, 1);

        drawPurificationTime(recipe, guiGraphics, 49);
    }

    private void drawPurificationTime(@NotNull PurificationRecipe recipe, @NotNull GuiGraphics guiGraphics, int y) {
        int purificationTime = recipe.getTime();
        if (purificationTime > 0) {
            int purificationSeconds = purificationTime / 20;
            Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", purificationSeconds);
            Minecraft minecraft = Minecraft.getInstance();
            Font fontRenderer = minecraft.font;
            int stringWidth = fontRenderer.width(timeString);
            guiGraphics.drawString(fontRenderer, timeString, getWidth() - stringWidth, y, 0xFF808080, false);
        }
    }

    @Override
    public @NotNull RecipeType<PurificationRecipe> getRecipeType() {
        return PURIFICATION_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.block.action.biosanity.purification_translatable");
    }
}