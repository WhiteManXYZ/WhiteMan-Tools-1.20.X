package net.whiteman.whitemantools.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.whiteman.whitemantools.WhiteManToolsMod;

public class PurificationStationBlockScreen extends AbstractContainerScreen<PurificationStationBlockMenu> {
    private static final ResourceLocation PURIFICATION_STATION_TEXTURE =
            new ResourceLocation(WhiteManToolsMod.MOD_ID, "textures/gui/purification_station_block_gui.png");
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
        renderFuelConvertationBubbles(guiGraphics, x, y);
        renderFuelBar(guiGraphics, x, y);
        renderModifierMaterialBar(guiGraphics, x, y);
        renderPressureBar(guiGraphics, x, y);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 92, y + 40, 176, 0, menu.getScaledProgress(), 16);
        }
    }

    private void renderFuelConvertationBubbles(GuiGraphics guiGraphics, int x, int y) {
        int progress = this.menu.getFuelConvertationProgress();
        
        int frame = BUBBLELENGTHS[progress / 4 % BUBBLELENGTHS.length];
        if (frame > 0) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 54, y + 51 + 10 - frame, 176, 39 - frame, 11, frame);
        }
    }

    private void renderFuelBar(GuiGraphics guiGraphics, int x, int y) {
        int fuel = this.menu.getFuel();
        int barWidth = Mth.clamp((18 * fuel + 20 - 1) / 20, 0, 18);
        if (barWidth > 0) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 50, y + 64, 176, 17, barWidth, 4);
        }
    }

    private void renderModifierMaterialBar(GuiGraphics guiGraphics, int x, int y) {
        int modifier_material = this.menu.getModifierMaterial();
        int barWidth = Mth.clamp((19 * modifier_material + 10 - 1) / 10, 0, 19);
        if (barWidth > 0) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 70, y + 17, 176, 21, barWidth, 8);
        }
    }

    private void renderPressureBar(GuiGraphics guiGraphics, int x, int y) {
        int pressure = this.menu.getPressure();

        if (pressure < 10)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 205, 0, 17, 17);
        if (pressure >= 10 && pressure < 20)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 205, 17, 17, 17);
        if (pressure >= 20 && pressure < 30)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 205, 34, 17, 17);
        if (pressure >= 30 && pressure < 40)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 205, 51, 17, 17);
        if (pressure >= 40 && pressure < 50)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 222, 0, 17, 17);
        if (pressure >= 50 && pressure < 60)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 222, 17, 17, 17);
        if (pressure >= 60 && pressure < 70)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 222, 34, 17, 17);
        if (pressure >= 70 && pressure < 80)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 222, 51, 17, 17);
        if (pressure >= 80 && pressure < 90)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 239, 0, 17, 17);
        if (pressure >= 90 && pressure < 100)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 239, 17, 17, 17);
        if (pressure >= 100 && pressure < 110)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 239, 34, 17, 17);
        if (pressure >= 110 && pressure < 120)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 239, 51, 17, 17);
        if (pressure >= 120 && pressure < 130)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 239, 68, 17, 17);
        if (pressure >= 130 && pressure < 140)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 239, 85, 17, 17);
        if (pressure >= 140)
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 152, y + 16, 239, 102, 17, 17);
    }
}
