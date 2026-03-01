package net.whiteman.biosanity.event;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.whiteman.biosanity.block.ModBlocks;
import net.whiteman.biosanity.goal.AvoidBlockGoal;

import java.util.function.Predicate;

@Mod.EventBusSubscriber(modid = "biosanity", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

    /**
     * Adds a custom AI Goal for selected vanilla mobs on their spawn.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;

        // Avoid selected blocks logic
        if (mob.getMobType() == MobType.UNDEAD) {
            // Mobs avoid only lit UV Lamp
            Predicate<BlockState> avoidPredicate = state ->
                    state.is(ModBlocks.UV_LAMP_BLOCK.get()) &&
                            state.hasProperty(BlockStateProperties.LIT) &&
                            state.getValue(BlockStateProperties.LIT);

            mob.goalSelector.addGoal(0, new AvoidBlockGoal(
                    mob,
                    avoidPredicate,
                    6.0,   // avoid radius X
                    6.0,                // avoid radius Y
                    1.2                 // speed to avoid
            ));
        }
    }
}
