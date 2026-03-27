package net.whiteman.biosanity.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OverlayBakedModel implements BakedModel {
    private final BakedModel fallbackModel;
    private final List<TextureAtlasSprite> overlaySprites;

    public OverlayBakedModel(BakedModel fallbackModel, List<TextureAtlasSprite> overlaySprites) {
        this.fallbackModel = fallbackModel;
        this.overlaySprites = overlaySprites;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        BlockState originalState = data.get(ModelProperties.ORIGINAL_STATE);

        // Fallback, if the block is not infected or the data is corrupted
        if (originalState == null || originalState.isAir()) {
            return fallbackModel.getQuads(state, side, rand, data, renderType);
        }

        if (side == null) return Collections.emptyList();

        BlockModelShaper blockModelShaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
        BakedModel originalModel = blockModelShaper.getBlockModel(originalState);

        List<BakedQuad> resultQuads = new ArrayList<>();

        // Adding basic quads (original block)
        if (renderType == null || originalModel.getRenderTypes(originalState, rand, data).contains(renderType)) {
            resultQuads.addAll(originalModel.getQuads(originalState, side, rand, data, renderType));
        }

        // Adding overlay quads (details)
        if (renderType == null || renderType == RenderType.translucent()) {
            Integer stage = data.get(ModelProperties.OVERLAY_STAGE);

            if (stage != null && stage >= 0 && stage < overlaySprites.size()) {
                TextureAtlasSprite overlaySprite = overlaySprites.get(stage);

                // We request quads from the original model
                RenderType primaryType = originalModel.getRenderTypes(originalState, rand, data).isEmpty()
                        ? RenderType.solid()
                        : originalModel.getRenderTypes(originalState, rand, data).iterator().next();

                List<BakedQuad> baseQuads = originalModel.getQuads(originalState, side, rand, data, primaryType);

                for (BakedQuad quad : baseQuads) {
                    resultQuads.add(createOverlayQuad(quad, overlaySprite));
                }
            }
        }

        return resultQuads;
    }

    private BakedQuad createOverlayQuad(BakedQuad baseQuad, TextureAtlasSprite newSprite) {
        int[] vertexData = baseQuad.getVertices().clone();
        int step = 8;
        TextureAtlasSprite baseSprite = baseQuad.getSprite();

        for (int i = 0; i < 4; i++) {
            int offset = i * step;

            // Position
            float x = Float.intBitsToFloat(vertexData[offset]);
            float y = Float.intBitsToFloat(vertexData[offset + 1]);
            float z = Float.intBitsToFloat(vertexData[offset + 2]);

            // UV-coords
            float u = Float.intBitsToFloat(vertexData[offset + 4]);
            float v = Float.intBitsToFloat(vertexData[offset + 5]);

            // Direct calculation of relative UV with clamp of 0..1
            float relU = (u - baseSprite.getU0()) / (baseSprite.getU1() - baseSprite.getU0());
            float relV = (v - baseSprite.getV0()) / (baseSprite.getV1() - baseSprite.getV0());

            // Restrict to go beyond the edges
            relU = Math.max(0, Math.min(1, relU));
            relV = Math.max(0, Math.min(1, relV));

            // New UV
            vertexData[offset + 4] = Float.floatToRawIntBits(newSprite.getU(relU * 16.0f));
            vertexData[offset + 5] = Float.floatToRawIntBits(newSprite.getV(relV * 16.0f));

            // Reset the color (offset + 3) to prevent the overlay from inheriting random colors (like grass)
            vertexData[offset + 3] = -1;
        }

        return new BakedQuad(vertexData, baseQuad.getTintIndex(), baseQuad.getDirection(), newSprite, baseQuad.isShade());
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    // BakedModel require this methods
    @Override
    public boolean useAmbientOcclusion() { return true; }
    @Override
    public boolean isGui3d() { return true; }
    @Override
    public boolean usesBlockLight() { return true; }
    @Override
    public boolean isCustomRenderer() { return false; }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return fallbackModel.getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState originalState = data.get(ModelProperties.ORIGINAL_STATE);
        if (originalState != null && !originalState.isAir()) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(originalState).getParticleIcon(data);
        }
        return fallbackModel.getParticleIcon(data);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() { return ItemOverrides.EMPTY; }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand, @NotNull ModelData data) {
        BlockState originalState = data.get(ModelProperties.ORIGINAL_STATE);

        // Fallback
        if (originalState == null) {
            return fallbackModel.getRenderTypes(state, rand, data);
        }

        ChunkRenderTypeSet originTypes = fallbackModel.getRenderTypes(originalState, rand, data);
        return ChunkRenderTypeSet.union(originTypes, ChunkRenderTypeSet.of(RenderType.translucent()));
    }
}