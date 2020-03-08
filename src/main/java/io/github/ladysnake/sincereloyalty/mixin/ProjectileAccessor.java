package io.github.ladysnake.sincereloyalty.mixin;

import net.minecraft.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(Projectile.class)
public interface ProjectileAccessor {
    @Accessor
    UUID getOwnerUuid();
}
