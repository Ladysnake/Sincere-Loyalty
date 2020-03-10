package io.github.ladysnake.sincereloyalty.storage;

import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class TridentEntry {
    @Nullable
    public static TridentEntry fromNbt(ServerWorld world, CompoundTag tag) {
        try {
            switch (tag.getString("type")) {
                case "world": return new WorldTridentEntry(world, tag);
                case "inventory": return new InventoryTridentEntry(world, tag);
                default: // pass
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected final ServerWorld world;
    protected final UUID tridentUuid;

    TridentEntry(ServerWorld world, UUID tridentUuid) {
        this.world = world;
        this.tridentUuid = tridentUuid;
    }

    TridentEntry(ServerWorld world, CompoundTag nbt) {
        this(world, nbt.method_25926("trident_uuid"));
    }

    public UUID getTridentUuid() {
        return tridentUuid;
    }

    @Nullable
    public abstract TridentEntity findTrident();

    public CompoundTag toNbt(CompoundTag nbt) {
        nbt.method_25927("trident_uuid", this.tridentUuid);
        return nbt;
    }
}
