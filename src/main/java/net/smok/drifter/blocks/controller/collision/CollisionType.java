package net.smok.drifter.blocks.controller.collision;

import com.mojang.serialization.Codec;

public interface CollisionType<C extends Collision> {

    Codec<C> codec();
}
