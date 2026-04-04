package net.whiteman.biosanity.world.neoplasm.core.hivemind;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum AlertLevel implements StringRepresentable {
    ///  WIP
    CALM("calm", 0),
    WATCHING("watching", 100),
    STRESSED("stressed", 1000),
    CRITICAL("critical", 2000);

    private final String name;
    private final int alertPoints;

    public static final AlertLevel[] ALERT_LEVELS = AlertLevel.values();

    AlertLevel(String name, int alertPoints) {
        this.name = name;
        this.alertPoints = alertPoints;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static AlertLevel getFromPoints(int points) {
        for (int i = ALERT_LEVELS.length - 1; i >= 0; i--) {
            if (points >= ALERT_LEVELS[i].alertPoints) {
                return ALERT_LEVELS[i];
            }
        }
        return CALM;
    }
}
