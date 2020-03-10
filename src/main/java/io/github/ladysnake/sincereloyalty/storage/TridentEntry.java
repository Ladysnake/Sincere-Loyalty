package io.github.ladysnake.sincereloyalty.storage;

import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public abstract class TridentEntry {
    @Nullable
    public static TridentEntry fromNbt(ServerWorld world, CompoundTag tag) {
        try {
            switch (tag.getString("type")) {
                case "world": return new WorldTridentEntry(world, tag);
                case "inventory": return new InventoryTridentEntry(world, tag);
                default:
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected final ServerWorld world;

    TridentEntry(ServerWorld world) {
        this.world = world;
    }

    @Nullable
    public abstract TridentEntity findTrident();

    public abstract CompoundTag toNbt(CompoundTag nbt);
}
