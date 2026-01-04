package net.smok.drifter.blocks.alert;

import java.util.List;

public interface Detector {

    List<Alert> getAllAlerts();

    void setChanged();

    default void setName(int alert, String name) {
        try {
            getAllAlerts().get(alert).setName(name);
        } catch (IndexOutOfBoundsException ignored) {}
    }

    default void setTest(int alert, boolean test) {
        try {
            getAllAlerts().get(alert).setTested(test);
        } catch (IndexOutOfBoundsException ignored) {}
    }

    default void moveAlert(int alert, int upOrDown) {
        try {
            Alert change = getAllAlerts().get(alert);
            getAllAlerts().set(alert, getAllAlerts().get(alert + upOrDown));
            getAllAlerts().set(alert + upOrDown, change);
        } catch (IndexOutOfBoundsException ignored) {}
    }

    default void setIcon(int alert, Icon icon) {
        try {
            getAllAlerts().get(alert).setIcon(icon);
        } catch (IndexOutOfBoundsException ignored) {}
    }
}
