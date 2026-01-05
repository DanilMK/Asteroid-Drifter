package net.smok.drifter.blocks.alert;

import java.util.List;

public interface Detector {

    List<Alert> getAllAlerts();

    void setChanged();

    default void setName(int alert, String name) {
        if (alert > getAllAlerts().size()) return;
        getAllAlerts().get(alert).setName(name);
    }

    default void setTest(int alert, boolean test) {
        if (alert > getAllAlerts().size()) return;
        getAllAlerts().get(alert).setTested(test);
    }

    default void moveAlert(int alert, int upOrDown) {
        int nextAlert = alert + upOrDown;
        if (alert > getAllAlerts().size() || nextAlert > getAllAlerts().size() || nextAlert < 0) return;

        Alert change = getAllAlerts().get(alert);
        getAllAlerts().set(alert, getAllAlerts().get(nextAlert));
        getAllAlerts().set(nextAlert, change);
    }

    default void setIcon(int alert, Icon icon) {
        if (alert > getAllAlerts().size()) return;
        getAllAlerts().get(alert).setIcon(icon);
    }

    default void setSound(int alert, AlertSound sound) {
        if (alert > getAllAlerts().size()) return;
        getAllAlerts().get(alert).setSound(sound);
    }
}
