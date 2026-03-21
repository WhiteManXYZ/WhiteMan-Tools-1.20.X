package net.whiteman.biosanity.compat.purification_station_categories;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ITickTimer;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.recipe.purification_station.AbstractStationRecipe;
import net.whiteman.biosanity.util.block.purification_station.ModifiersUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCategory<T extends AbstractStationRecipe> implements IRecipeCategory<T> {
    public static final ResourceLocation PURIFICATION_STATION_TEXTURE = new ResourceLocation(BiosanityMod.MOD_ID,
            "textures/gui/purification_station_block_gui.png");

    protected final IDrawable background;
    protected final IDrawable icon;
    protected final IDrawable fuelIcon;
    protected final IDrawable pressureIcon;
    protected final IDrawable modifierIcon;
    protected final IDrawable sandModifierIcon;
    protected final IDrawableAnimated bubbles;
    private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;

    public AbstractCategory(IGuiHelper helper, int pressureIconU, int pressureIconV) {
        this.background = helper.createDrawable(PURIFICATION_STATION_TEXTURE, 48, 16, 121, 58);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.PURIFICATION_STATION_BLOCK.get()));
        this.pressureIcon = helper.createDrawable(PURIFICATION_STATION_TEXTURE, pressureIconU, pressureIconV, 17, 17);

        ITickTimer bubblesTickTimer = new BubblesTickTimer(helper);
        bubbles = helper.drawableBuilder(PURIFICATION_STATION_TEXTURE, 176, 29, 11, 11)
                .buildAnimated(bubblesTickTimer, IDrawableAnimated.StartDirection.BOTTOM);

        this.fuelIcon = helper.createDrawable(PURIFICATION_STATION_TEXTURE, 176, 17, 12, 4);
        this.modifierIcon = helper.createDrawable(PURIFICATION_STATION_TEXTURE, 176, 21, 19, 8);
        this.sandModifierIcon = helper.createDrawable(PURIFICATION_STATION_TEXTURE, 176, 40, 19, 8);

        this.cachedArrows = CacheBuilder.newBuilder()
                .maximumSize(25)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull IDrawableAnimated load(@NotNull Integer cookTime) {
                        return helper.drawableBuilder(PURIFICATION_STATION_TEXTURE, 176, 0, 24, 17)
                                .buildAnimated(cookTime, IDrawableAnimated.StartDirection.LEFT, false);
                    }
                });
    }

    @Override
    public void draw(@NotNull T recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IDrawableAnimated arrow = getArrow(recipe);
        arrow.draw(guiGraphics, 44, 23);
        // Render necessary pressure for specific item
        if (mouseX >= 104 && mouseX <= 120 && mouseY >= 0 && mouseY <= 16) {
            ModifiersUtils.ModifierType type = recipe.getModifier();
            int pressure = ModifiersUtils.ModifierManager.getNecessaryPressure(type);
            List<Component> tooltip = new ArrayList<>();

            tooltip.add(Component.translatable("jei.biosanity.purification_station_block.necessary_pressure_label")
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.translatable("jei.biosanity.purification_station_block.necessary_pressure_value", pressure)
                    .withStyle(ChatFormatting.AQUA));

            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, (int) mouseX, (int) mouseY);
        }
    }

    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, @NotNull IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 24).addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 79, 24).addItemStack(recipe.getResultItem(RegistryAccess.EMPTY));
    }

    public @NotNull IDrawable getBackground() {
        return this.background;
    }

    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    protected IDrawableAnimated getArrow(T recipe) {
        int cookTime = recipe.getTime();
        if (cookTime <= 0) {
            cookTime = 200;
        }
        return this.cachedArrows.getUnchecked(cookTime);
    }

    protected static class BubblesTickTimer implements ITickTimer {
        private static final int[] BUBBLE_LENGTHS = new int[]{11, 5, 0};
        private final ITickTimer internalTimer;

        public BubblesTickTimer(IGuiHelper guiHelper) {
            this.internalTimer = guiHelper.createTickTimer(12, BUBBLE_LENGTHS.length - 1, false);
        }

        @Override
        public int getValue() {
            int timerValue = this.internalTimer.getValue();
            return BUBBLE_LENGTHS[timerValue];
        }

        @Override
        public int getMaxValue() {
            return BUBBLE_LENGTHS[0];
        }
    }
}
