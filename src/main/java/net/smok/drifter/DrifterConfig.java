package net.smok.drifter;

import com.teamresourceful.resourcefulconfig.common.annotations.Comment;
import com.teamresourceful.resourcefulconfig.common.annotations.Config;
import com.teamresourceful.resourcefulconfig.common.annotations.ConfigEntry;
import com.teamresourceful.resourcefulconfig.common.annotations.InlineCategory;
import com.teamresourceful.resourcefulconfig.common.config.EntryType;

@Config(value = "drifter_config")
public final class DrifterConfig {


    @ConfigEntry(
            id = "startOnShip",
            type = EntryType.BOOLEAN,
            translation = "config.drifter.start_on_ship"
    )
    @Comment(value = "Set world spawn point to Asteroids dimension", translation = "config.drifter.start_on_ship.comment")
    public static boolean startOnShip;

    @InlineCategory
    public static ShipConfig shipConfig = new ShipConfig();
}
