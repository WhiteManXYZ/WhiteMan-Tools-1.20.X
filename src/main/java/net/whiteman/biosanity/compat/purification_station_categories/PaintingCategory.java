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
import net.whiteman.biosanity.recipe.purification_station.PaintingRecipe;
import org.jetbrains.annotations.NotNull;

import static net.whiteman.biosanity.util.block.purification_station.ModifiersUtils.*;

public class PaintingCategory extends AbstractCategory<PaintingRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(BiosanityMod.MOD_ID, "painting");

    public static final RecipeType<PaintingRecipe> PAINTING_TYPE =
            new RecipeType<>(UID, PaintingRecipe.class);

    public PaintingCategory(IGuiHelper helper) {
        super(helper, 205, 0);
    }

    public void draw(@NotNull PaintingRecipe recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        bubbles.draw(guiGraphics, 6, 35);
        pressureIcon.draw(guiGraphics, 104, 0);
        fuelIcon.draw(guiGraphics, 2, 48);
        renderModifierMaterialBar(recipe, guiGraphics, 22, 1);
        drawPaintingTime(recipe, guiGraphics, 49);
    }

    private void drawPaintingTime(@NotNull PaintingRecipe recipe, @NotNull GuiGraphics guiGraphics, int y) {
        int paintingTime = recipe.getTime();
        if (paintingTime > 0) {
            int paintingSeconds = paintingTime / 20;
            Component timeString = Component.translatable("gui.jei.category.smelting.time.seconds", paintingSeconds);
            Minecraft minecraft = Minecraft.getInstance();
            Font fontRenderer = minecraft.font;
            int stringWidth = fontRenderer.width(timeString);
            guiGraphics.drawString(fontRenderer, timeString, getWidth() - stringWidth, y, 0xFF808080, false);
        }
    }

    private void renderModifierMaterialBar(@NotNull PaintingRecipe recipe, GuiGraphics guiGraphics, int x, int y) {
        float[] color = adjustColor(convertToColors(recipe.getColor()), 0.35f, -0.35f);

        guiGraphics.setColor(color[0], color[1], color[2], 1.0f);
        guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x, y, 176, 21, 19, 8);
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public @NotNull RecipeType<PaintingRecipe> getRecipeType() {
        return PAINTING_TYPE;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("jei.block.action.biosanity.painting_translatable");
    }
}