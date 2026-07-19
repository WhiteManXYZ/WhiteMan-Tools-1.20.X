package net.whiteman.biosanity.world.neoplasm.ai;

import net.whiteman.biosanity.world.neoplasm.core.NeoplasmCoreBE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class GoalRegistry {
    private static final List<Function<NeoplasmCoreBE, IHivemindGoal>> GOAL_FACTORIES = new ArrayList<>();

    static {
        // Growth & expansion
        register(core -> new GrowVeinGoal(core, 20d, 140));
        register(core -> new GrowNewVeinGoal(core, 25d, 200));
        register(core -> new ExpandHivemindGoal(core, 20d, 420));

        // Idle
        register(core -> new IdleGoal(core, 10d, 20));
    }

    public static void register(Function<NeoplasmCoreBE, IHivemindGoal> factory) {
        GOAL_FACTORIES.add(factory);
    }

    /** Creates a fresh list of all available tasks for a particular core */
    public static List<IHivemindGoal> createGoalsFor(NeoplasmCoreBE core) {
        List<IHivemindGoal> instantiatedGoals = new ArrayList<>();

        for (var factory : GOAL_FACTORIES) {
            IHivemindGoal goal = factory.apply(core);
            if (goal != null) {
                instantiatedGoals.add(goal);
            }
        }

        instantiatedGoals.sort(Comparator.comparingDouble(IHivemindGoal::getBaseWeight));

        return instantiatedGoals;
    }

    public static List<Function<NeoplasmCoreBE, IHivemindGoal>> getFactories() {
        return Collections.unmodifiableList(GOAL_FACTORIES);
    }
}