package net.whiteman.biosanity.util.block.purification_station;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ColorsRegistry {
    public static final Map<DyeColor, TextColor> DYE_TO_COLOR = new EnumMap<>(DyeColor.class);

    private static void register(DyeColor dye, ChatFormatting format) {
        Integer colorValue = format.getColor();

        if (colorValue == null) {
            throw new IllegalArgumentException("Error in ColorsRegistry: Formatting " + format.name() +
                    " has no color! Don't use styles such of (BOLD/ITALIC) instead colors.");
        }

        DYE_TO_COLOR.put(dye, TextColor.fromRgb(colorValue));
    }

    private static void register(DyeColor dye, int hexColor) {
        DYE_TO_COLOR.put(dye, TextColor.fromRgb(hexColor));
    }

    static {
        register(DyeColor.WHITE, ChatFormatting.WHITE);
        register(DyeColor.ORANGE, ChatFormatting.GOLD);
        register(DyeColor.MAGENTA, 0xC74EBD);
        register(DyeColor.LIGHT_BLUE, ChatFormatting.BLUE);
        register(DyeColor.YELLOW, ChatFormatting.YELLOW);
        register(DyeColor.LIME, ChatFormatting.GREEN);
        register(DyeColor.PINK, ChatFormatting.LIGHT_PURPLE);
        register(DyeColor.GRAY, ChatFormatting.DARK_GRAY);
        register(DyeColor.LIGHT_GRAY, ChatFormatting.GRAY);
        register(DyeColor.CYAN, ChatFormatting.DARK_AQUA);
        register(DyeColor.PURPLE, ChatFormatting.DARK_PURPLE);
        register(DyeColor.BLUE, ChatFormatting.DARK_BLUE);
        register(DyeColor.BROWN, 0x835432);
        register(DyeColor.GREEN, ChatFormatting.DARK_GREEN);
        register(DyeColor.RED, ChatFormatting.RED);
        register(DyeColor.BLACK, 0x373737);
    }

    public static final List<Item> WOOLS = getAllColorsFor("_wool");
    public static final List<Item> WOOL_CARPETS = getAllColorsFor("_carpet");
    public static final List<Item> CONCRETE = getAllColorsFor("_concrete");
    public static final List<Item> CONCRETE_POWDER = getAllColorsFor("_concrete_powder");
    public static final List<Item> BEDS = getAllColorsFor("_bed");
    public static final List<Item> BANNERS = getAllColorsFor("_banner");
    public static final List<Item> GLASS = Stream.concat(
            Stream.of(Items.GLASS),
            getAllColorsFor("_stained_glass").stream()
    ).toList();
    public static final List<Item> GLASS_PANES = Stream.concat(
            Stream.of(Items.GLASS_PANE),
            getAllColorsFor("_stained_glass_pane").stream()
    ).toList();
    public static final List<Item> CANDLE = Stream.concat(
            Stream.of(Items.CANDLE),
            getAllColorsFor("_candle").stream()
    ).toList();
    public static final List<Item> SHULKERS = Stream.concat(
            Stream.of(Items.SHULKER_BOX),
            getAllColorsFor("_shulker_box").stream()
    ).toList();
    public static final List<Item> TERRACOTTA = Stream.concat(
            Stream.of(Items.TERRACOTTA),
            getAllColorsFor("_terracotta").stream()
    ).toList();

    public static List<Item> getAllColorsFor(String suffix) {
        List<Item> items = new ArrayList<>();
        for (DyeColor color : DyeColor.values()) {
            ResourceLocation location = new ResourceLocation("minecraft", color.getName() + suffix);
            Item item = ForgeRegistries.ITEMS.getValue(location);
            if (item != Items.AIR) {
                items.add(item);
            }
        }
        return List.copyOf(items);
    }

    public static Item getBlockForColor(List<Item> family, DyeColor color) {
        String colorName = color.getName();
        return family.stream()
                .filter(item -> {
                    ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                    return key != null && key.getPath().startsWith(colorName);
                })
                .findFirst()
                .orElse(Items.AIR);
    }

    public static boolean isPaintable(ItemStack stack) {
        Item item = stack.getItem();
        return WOOLS.contains(item) ||
                WOOL_CARPETS.contains(item) ||
                CONCRETE.contains(item) ||
                CONCRETE_POWDER.contains(item) ||
                BEDS.contains(item) ||
                BANNERS.contains(item) ||
                GLASS.contains(item) ||
                GLASS_PANES.contains(item) ||
                CANDLE.contains(item) ||
                SHULKERS.contains(item) ||
                TERRACOTTA.contains(item);
    }

    public static List<Item> getListByFolder(String folder) {
        return switch (folder) {
            case "wool" -> ColorsRegistry.WOOLS;
            case "wool_carpets" -> ColorsRegistry.WOOL_CARPETS;
            case "glass" -> ColorsRegistry.GLASS;
            case "glass_panes" -> ColorsRegistry.GLASS_PANES;
            case "concrete_powder" -> ColorsRegistry.CONCRETE_POWDER;
            case "concrete" -> ColorsRegistry.CONCRETE;
            case "candle" -> ColorsRegistry.CANDLE;
            case "terracotta" -> ColorsRegistry.TERRACOTTA;
            case "shulkers" -> ColorsRegistry.SHULKERS;
            case "beds" -> ColorsRegistry.BEDS;
            case "banners" -> ColorsRegistry.BANNERS;
            default -> List.of();
        };
    }
}