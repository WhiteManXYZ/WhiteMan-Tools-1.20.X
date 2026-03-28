package net.whiteman.biosanity.block.custom.neoplasm;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public class NeoplasmBlock extends Block {
    public NeoplasmBlock(Properties pProperties) { super(pProperties
            .strength(1.0F, 9.0F)
            .friction(0.8F)
            .sound(SoundType.SLIME_BLOCK)
            .mapColor(MapColor.TERRACOTTA_RED));
    }
}
