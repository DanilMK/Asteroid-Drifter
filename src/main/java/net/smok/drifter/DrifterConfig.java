package net.smok.drifter;

import com.teamresourceful.resourcefulconfig.common.annotations.Config;
import com.teamresourceful.resourcefulconfig.common.annotations.InlineCategory;

@Config(value = "drifter_config")
public final class DrifterConfig {

    @InlineCategory
    public static ShipConfig shipConfig = new ShipConfig();
}
