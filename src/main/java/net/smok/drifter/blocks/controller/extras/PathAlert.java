package net.smok.drifter.blocks.controller.extras;

import net.smok.drifter.blocks.alert.Alert;
import net.smok.drifter.blocks.alert.Detector;
import net.smok.drifter.blocks.alert.Icon;

public class PathAlert extends Alert {


    public PathAlert(Detector detector, String defaultName) {
        super(detector, defaultName);
    }

    public PathAlert(Detector detector, Icon defaultIcon, String defaultName) {
        super(detector, defaultIcon, defaultName);
    }


}
