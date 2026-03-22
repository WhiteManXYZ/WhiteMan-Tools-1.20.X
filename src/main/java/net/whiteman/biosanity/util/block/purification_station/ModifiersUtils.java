package net.whiteman.biosanity.util.block.purification_station;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.whiteman.biosanity.item.ModItems;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ModifiersUtils {
    public enum ModifierType {
        NONE("modifiertypes.biosanity.purification_station_block.none"),
        SAND_DUST("modifiertypes.biosanity.purification_station_block.sand_dust"),
        DYE("modifiertypes.biosanity.purification_station_block.dye");

        private final String name;

        ModifierType(String name) {
            this.name = name;
        }

        public @NotNull String getTranslatableName() {
            return this.name;
        }
    }

    public static int packColor(float[] colors) {
        if (colors == null) return 0;

        // Pack color
        int r = (int) (colors[0] * 255.0F);
        int g = (int) (colors[1] * 255.0F);
        int b = (int) (colors[2] * 255.0F);

        return (255 << 24) | (r << 16) | (g << 8) | b;
    }

    public static float[] unpackColor(int color) {
        // If color has no alpha channel (0xRRGGBB), add it (0xFFRRGGBB)
        // Otherwise setColor can make texture translucent
        if ((color >> 24 & 0xFF) == 0) {
            color |= 0xFF000000;
        }

        // Unpack color
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        float a = ((color >> 24) & 0xFF) / 255.0f;

        return new float[] {r, g, b, a};
    }

    public static float[] adjustColor(float[] colors, float lightness, float desaturate) {
        if (colors == null) return new float[] {0, 0, 0};
        // Unpack color
        float r = colors[0];
        float g = colors[1];
        float b = colors[2];

        // Decrease saturation
        float luminance = (r + g + b) / 3.0f;
        r = r + (luminance - r) * desaturate;
        g = g + (luminance - g) * desaturate;
        b = b + (luminance - b) * desaturate;

        // Lightening
        r = r + (1.0f - r) * lightness;
        g = g + (1.0f - g) * lightness;
        b = b + (1.0f - b) * lightness;

        // Pack back
        return new float[] { r, g, b };
    }

    public static float[] convertToColors(DyeColor color) {
        return color == null ? null : color.getTextureDiffuseColors();
    }

    public record ModifierData(int capacity, DyeColor color, ModifierType type, int necessary_pressure) {}

    public static class ModifierManager {
        private record TypeSettings(int capacity, int pressure) {}
        private static final Map<ModifierType, TypeSettings> TYPE_CHARACTERISTICS = new EnumMap<>(ModifierType.class);
        private static final Map<Item, ModifierData> MODIFIERS = new HashMap<>();

        static {
            TYPE_CHARACTERISTICS.put(ModifierType.SAND_DUST, new TypeSettings(4, 30));
            TYPE_CHARACTERISTICS.put(ModifierType.DYE, new TypeSettings(8, 5));
            // Sand dust registry
            TypeSettings sandSettings = TYPE_CHARACTERISTICS.get(ModifierType.SAND_DUST);
            register(ModItems.SAND_DUST.get(), sandSettings.capacity, DyeColor.WHITE, ModifierType.SAND_DUST, sandSettings.pressure);
            // Dye registry
            TypeSettings dyeSettings = TYPE_CHARACTERISTICS.get(ModifierType.DYE);
            for (DyeColor color : DyeColor.values()) {
                Item dyeItem = DyeItem.byColor(color);
                register(dyeItem, dyeSettings.capacity, color, ModifierType.DYE, dyeSettings.pressure);
            }
        }

        private static void register(Item item, int cap, DyeColor color, ModifierType type, int necessary_pressure) {
            MODIFIERS.put(item, new ModifierData(cap, color, type, necessary_pressure));
        }

        public static ModifierData get(Item item) {
            return MODIFIERS.getOrDefault(item, new ModifierData(0, DyeColor.WHITE, ModifierType.NONE, Integer.MAX_VALUE));
        }

        public static DyeColor getColor(Item item) {
            return get(item).color();
        }

        public static int getNecessaryPressure(ModifierType type) {
            TypeSettings settings = TYPE_CHARACTERISTICS.get(type);
            return settings != null ? settings.pressure : Integer.MAX_VALUE;
        }

        public static int getCapacity(ModifierType type) {
            TypeSettings settings = TYPE_CHARACTERISTICS.get(type);
            return settings != null ? settings.capacity : Integer.MAX_VALUE;
        }
    }
}
