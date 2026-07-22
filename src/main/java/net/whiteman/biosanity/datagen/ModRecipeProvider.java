package net.whiteman.biosanity.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import net.whiteman.biosanity.BiosanityMod;
import net.whiteman.biosanity.world.level.block.ModBlocks;
import net.whiteman.biosanity.datagen.recipe_builders.PurificationStationRecipeBuilder;
import net.whiteman.biosanity.world.item.ModItems;
import net.whiteman.biosanity.world.item.crafting.ModRecipes;
import net.whiteman.biosanity.world.util.ColoredItemsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static net.whiteman.biosanity.world.util.ModifierUtils.ModifierType;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    private static final List<ItemLike> ALGANIT_SMELTABLES = List.of(ModBlocks.NETHER_ALGANIT_ORE.get());
    private static final String PURIFICATION_PATH = "purifier_recipes/purification/";
    private static final String PAINTING_PATH = "purifier_recipes/painting/";

    private static final Map<String, TagKey<Item>> PAINTING_FOLDERS = Map.ofEntries(
            Map.entry("wool", ItemTags.WOOL),
            Map.entry("wool_carpets", ItemTags.WOOL_CARPETS),
            Map.entry("glass", Tags.Items.GLASS),
            Map.entry("concrete_powder", Tags.Items.SAND),
            Map.entry("glass_panes", Tags.Items.GLASS_PANES),
            Map.entry("candle", ItemTags.CANDLES),
            Map.entry("shulkers", ItemTags.create(new ResourceLocation("minecraft", "shulker_boxes"))),
            Map.entry("terracotta", ItemTags.TERRACOTTA),
            Map.entry("beds", ItemTags.BEDS),
            Map.entry("banners", ItemTags.BANNERS),
            Map.entry("concrete", ModTags.Items.CONCRETE)
    );

    private static final Set<Item> EXCLUDED_ITEMS = Set.of(Items.TINTED_GLASS);

    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        //region ------------ Vanilla recipes datagen
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.UV_LAMP_BLOCK.get())
                .pattern("IAI")
                .pattern("A#A")
                .pattern("IAI")
                .define('A', ModItems.PURIFIED_ALGANIT.get())
                .define('I', Items.IRON_INGOT)
                .define('#', Items.REDSTONE_LAMP)
                .unlockedBy(getHasName(ModItems.PURIFIED_ALGANIT.get()), has(ModItems.PURIFIED_ALGANIT.get()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Blocks.SAND)
                .pattern("SS")
                .pattern("SS")
                .define('S', ModItems.SAND_DUST.get())
                .unlockedBy(getHasName(Blocks.SAND), has(Blocks.SAND))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SAND_DUST.get(), 4)
                .requires(Blocks.SAND)
                .unlockedBy(getHasName(Blocks.SAND), has(Blocks.SAND))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.PURIFICATION_STATION_BLOCK.get())
                .pattern("OOO")
                .pattern("O#O")
                .pattern("O O")
                .define('O', Items.IRON_INGOT)
                .define('#', Items.IRON_BLOCK)
                .unlockedBy(getHasName(Items.IRON_BLOCK), has(Items.IRON_BLOCK))
                .save(pWriter);

        oreSmelting(pWriter, ALGANIT_SMELTABLES, RecipeCategory.MISC, ModItems.ALGANIT.get(), 0, 200, "alganit");
        oreBlasting(pWriter, ALGANIT_SMELTABLES, RecipeCategory.MISC, ModItems.ALGANIT.get(), 0, 100, "alganit");
        //endregion
        
        //region ------------ Custom recipes datagen
        PurificationStationRecipeBuilder.recipe(
                ModItems.PURIFIED_ALGANIT.get(),
                Ingredient.of(ModItems.ALGANIT.get()),
                ModifierType.SAND_DUST,
                1,
                400,
                DyeColor.WHITE,
                ModRecipes.PURIFICATION_SERIALIZER.get()
        ).save(pWriter, new ResourceLocation(BiosanityMod.MOD_ID,
                PURIFICATION_PATH + "purification_" + ModItems.ALGANIT.get()));

        for (DyeColor color : DyeColor.values()) {
            PAINTING_FOLDERS.forEach((folderName, tag) -> {
                List<Item> folderItems = ColoredItemsRegistry.getListByFolder(folderName);
                Item result = ColoredItemsRegistry.getBlockForColor(folderItems, color);

                if (result != Items.AIR) {
                    List<Item> filteredItems = folderItems.stream()
                            .filter(item -> !EXCLUDED_ITEMS.contains(item))
                            .filter(item -> item != result)
                            .toList();

                    if (!filteredItems.isEmpty()) {
                        Ingredient listIngredient = Ingredient.of(filteredItems.toArray(new Item[0]));
                        PurificationStationRecipeBuilder.recipe(
                                result,
                                listIngredient,
                                ModifierType.DYE,
                                1,
                                100,
                                color,
                                ModRecipes.PAINTING_SERIALIZER.get()
                        ).save(pWriter, new ResourceLocation(BiosanityMod.MOD_ID,
                                PAINTING_PATH + folderName + "/" + color.getName()));
                    }
                }
            });
        }
        //endregion
    }

    protected static void oreSmelting(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTIme, @NotNull String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.SMELTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTime, @NotNull String pGroup) {
        oreCooking(pFinishedRecipeConsumer, RecipeSerializer.BLASTING_RECIPE, pIngredients, pCategory, pResult, pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static void oreCooking(@NotNull Consumer<FinishedRecipe> pFinishedRecipeConsumer, @NotNull RecipeSerializer<? extends AbstractCookingRecipe> pCookingSerializer, List<ItemLike> pIngredients, @NotNull RecipeCategory pCategory, @NotNull ItemLike pResult, float pExperience, int pCookingTime, @NotNull String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult,
                    pExperience, pCookingTime, pCookingSerializer)
                    .group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pFinishedRecipeConsumer,  BiosanityMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
