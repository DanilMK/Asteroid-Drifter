package net.smok.drifter.world;

import com.mojang.serialization.Codec;

public interface ModifierType<M extends Modifier> {

    Codec<M> codec();
}
