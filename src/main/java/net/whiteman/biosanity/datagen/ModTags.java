package net.whiteman.biosanity.datagen;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.whiteman.biosanity.BiosanityMod;

public class ModTags {
    public static class Items {
        public static final TagKey<Item> CONCRETE = tag("concrete");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(BiosanityMod.MOD_ID, name));
        }
    }
}