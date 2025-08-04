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
        renderFuelBar(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 92, y + 40, 176, 0, menu.getScaledProgress(), 16);
        }
    }

    private void renderFuelBar(GuiGraphics guiGraphics, int x, int y) {
        int fuel = this.menu.getFuel();
        int barWidth = Mth.clamp((18 * fuel + 20 - 1) / 20, 0, 18);
        if (barWidth > 0) {
            guiGraphics.blit(PURIFICATION_STATION_TEXTURE, x + 50, y + 64, 176, 17, barWidth, 4);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
