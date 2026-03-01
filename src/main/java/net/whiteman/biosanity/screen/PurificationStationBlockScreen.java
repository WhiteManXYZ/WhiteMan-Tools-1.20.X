package net.whiteman.biosanity.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.block.entity.PurificationStationBlockEntity;
import org.jetbrains.annotations.NotNull;

public class PurificationStationBlockScreen extends AbstractContainerScreen<PurificationStationBlockMenu> {
    private static final ResourceLocation PURIFICATION_STATION_TEXTURE =
            new ResourceLocation(BiosanityMod.MOD_ID, "textures/gui/purification_station_block_gui.png");
    private static final int[] BUBBLELENGTHS = new int[]{0, 5, 10};

    public PurificationStationBlockScreen(PurificationStationBlockMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, PURIFICATION_STATION_TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderProgressArrow(guiGraphics, x, y);
        renderFuelConversionBubbles(guiGraphics, x, y);
        renderFuelBar(guiGraphics, x, y);
        renderModifierMaterialBar(guiGraphics, x, y);
        renderPressureBar(guiGraphics, x, y);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 92, y + 40, 176, 0, menu.getScaledProgress(), 16);
        }
    }

    private void renderFuelConversionBubbles(GuiGraphics guiGraphics, int x, int y) {
        int progress = this.menu.getFuelConversionProgress();
        
        int frame = BUBBLELENGTHS[progress / 4 % BUBBLELENGTHS.length];
        if (frame > 0) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 54, y + 51 + 10 - frame, 176, 39 - frame, 11, frame);
        }
    }

    private void renderFuelBar(GuiGraphics guiGraphics, int x, int y) {
        int fuel = this.menu.getFuel();
        int fuel_max_count = PurificationStationBlockEntity.MAX_FUEL_COUNT;
        int barWidth = Mth.clamp((18 * fuel + fuel_max_count - 1) / fuel_max_count, 0, 18);
        if (barWidth > 0) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 50, y + 64, 176, 17, barWidth, 4);
        }
    }

    private void renderModifierMaterialBar(GuiGraphics guiGraphics, int x, int y) {
        int modifier_material = this.menu.getModifierMaterial();
        int modifier_max_count = PurificationStationBlockEntity.MAX_MODIFIER_COUNT;
        int barWidth = Mth.clamp((19 * modifier_material + modifier_max_count - 1) / modifier_max_count, 0, 19);
        if (barWidth > 0) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 70, y + 17, 176, 21, barWidth, 8);
        }
    }

    private void renderPressureBar(GuiGraphics guiGraphics, int x, int y) {
        int pressure = this.menu.getPressure();

        // Get the frame index. Divide by 10, limiting to a maximum of frame 13
        // TODO(whiteman) REMOVE USELESS FRAME FROM GUI TEXTURE
        int frame = Math.min(pressure / 10, 13);
        // Determine in which column (U) our frame is located
        int column = Math.min(frame / 4, 2);
        // Resolving U coordinate
        int u = 205 + (column * 17);
        // Resolving V coordinate
        int v;
        if (column < 2) {
            v = (frame % 4) * 17;
        } else {
            v = (frame - 8) * 17;
        }

        guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, u, v, 17, 17);
    }
}
