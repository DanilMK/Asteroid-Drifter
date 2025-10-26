package net.smok.drifter;

import com.teamresourceful.resourcefulconfig.client.ConfigScreen;
import com.teamresourceful.resourcefulconfig.common.config.ResourcefulConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.smok.drifter.registries.Values;

public class ModMenuConfig implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return screen -> {
            ResourcefulConfig config = Values.CONFIGURATOR.getConfig(DrifterConfig.class);
            if (config == null) return null;
            return new ConfigScreen(screen, null, config);
        };
    }
}
