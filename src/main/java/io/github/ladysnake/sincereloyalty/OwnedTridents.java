package io.github.ladysnake.sincereloyalty;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class OwnedTridents implements Iterable<TridentEntity> {
    static final OwnedTridents EMPTY = new OwnedTridents();

    private final LoyalTridentStorage parentStorage;
    private final Map<UUID, BlockPos> ownedTridents;

    private OwnedTridents() {
        this.parentStorage = null;
        this.ownedTridents = Collections.emptyMap();
    }

    OwnedTridents(LoyalTridentStorage parentStorage) {
        this.parentStorage = parentStorage;
        ownedTridents = new HashMap<>();
    }

    public void storeTridentPosition(UUID tridentUuid, BlockPos pos) {
        this.ownedTridents.put(tridentUuid, pos);
    }

    public void clearTridentPosition(UUID tridentUuid) {
        this.ownedTridents.remove(tridentUuid);
    }

    @NotNull
    @Override
    public Iterator<TridentEntity> iterator() {
        return new Iterator<TridentEntity>() {
            private final Iterator<Map.Entry<UUID, BlockPos>> iterator = ownedTridents.entrySet().iterator();
            private @Nullable TridentEntity next = null;

            private TridentEntity findNext() {
                while (this.next == null && iterator.hasNext()) {
                    Map.Entry<UUID, BlockPos> entry = iterator.next();
                    UUID uuid = entry.getKey();
                    BlockPos pos = entry.getValue();
                    // preload the chunk
                    parentStorage.world.getChunk(pos);
                    Entity trident = parentStorage.world.getEntity(uuid);
                    if (trident instanceof TridentEntity) {
                        this.next = (TridentEntity) trident;
                    } else {
                        iterator.remove();
                    }
                }
                return this.next;
            }

            @Override
            public boolean hasNext() {
                return findNext() != null;
            }

            @Override
            public TridentEntity next() {
                TridentEntity next = this.findNext();
                if (next == null) {
                    throw new NoSuchElementException();
                }
                this.next = null;
                return next;
            }
        };
    }

    public boolean isEmpty() {
        return this.ownedTridents.isEmpty();
    }

    public void fromTag(CompoundTag ownerNbt) {
        ListTag tridentsNbt = ownerNbt.getList("tridents", NbtType.COMPOUND);
        for (int j = 0; j < tridentsNbt.size(); j++) {
            CompoundTag tridentNbt = tridentsNbt.getCompound(j);
            UUID tridentUuid = tridentNbt.method_25926("trident_uuid");
            storeTridentPosition(tridentUuid, NbtHelper.toBlockPos(tridentNbt));
        }
    }

    public void toTag(CompoundTag ownerNbt) {
        ListTag tridentsNbt = new ListTag();
        this.ownedTridents.forEach((uuid, pos) -> {
            CompoundTag tridentNbt = NbtHelper.fromBlockPos(pos);
            tridentNbt.method_25927("trident_uuid", uuid);
            tridentsNbt.add(tridentNbt);
        });
        ownerNbt.put("tridents", tridentsNbt);
    }
}
