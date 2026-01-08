package net.smok.drifter.blocks.alert;

import net.minecraft.core.BlockPos;

import java.util.List;

public interface Detector {

    List<Alert> getAllAlerts();

    void setChanged();

    BlockPos getBlockPos();

    default boolean isExtreme () {
        return true;
    }

    default void setName(int alert, String name) {
        if (alert > alertsSize()) return;
        getAllAlerts().get(alert).setName(name);
    }

    default int alertsSize() {
        return getAllAlerts().size();
    }

    default void setTest(int alert, boolean test) {
        if (alert > alertsSize()) return;
        getAllAlerts().get(alert).setTested(test);
    }

    default void swap(int alertA, int alertB) {
        if (alertA >= alertsSize() || alertA < 0 || alertB >= alertsSize() || alertB < 0) return;

        Alert change = getAllAlerts().get(alertA);
        getAllAlerts().set(alertA, getAllAlerts().get(alertB));
        getAllAlerts().set(alertB, change);
    }

    default void setIcon(int alert, Icon icon) {
        if (alert > alertsSize()) return;
        getAllAlerts().get(alert).setIcon(icon);
    }

    default void setSound(int alert, AlertSound sound) {
        if (alert > alertsSize()) return;
        getAllAlerts().get(alert).setSound(sound);
    }
}
