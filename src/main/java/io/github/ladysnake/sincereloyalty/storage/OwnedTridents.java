/*
 * Sincere Loyalty
 * Copyright (C) 2020 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
package io.github.ladysnake.sincereloyalty.storage;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class OwnedTridents implements Iterable<TridentEntry> {
    static final OwnedTridents EMPTY = new OwnedTridents();

    private final LoyalTridentStorage parentStorage;
    private final Map<UUID, TridentEntry> ownedTridents;

    private OwnedTridents() {
        this.parentStorage = null;
        this.ownedTridents = Collections.emptyMap();
    }

    OwnedTridents(LoyalTridentStorage parentStorage) {
        this.parentStorage = parentStorage;
        ownedTridents = new HashMap<>();
    }

    public void storeTridentPosition(UUID tridentUuid, UUID tridentEntityUuid, BlockPos lastPos) {
        TridentEntry entry = this.ownedTridents.get(tridentUuid);
        if (entry instanceof WorldTridentEntry) {
            ((WorldTridentEntry) entry).updateLastPos(tridentEntityUuid, lastPos);
        } else {
            this.ownedTridents.put(tridentUuid, new WorldTridentEntry(this.parentStorage.world, tridentUuid, lastPos));
        }
    }

    public void storeTridentHolder(UUID tridentUuid, PlayerEntity holder) {
        TridentEntry entry = this.ownedTridents.get(tridentUuid);
        if (!(entry instanceof InventoryTridentEntry) || !((InventoryTridentEntry) entry).isHolder(holder)) {
            this.ownedTridents.put(tridentUuid, new InventoryTridentEntry(this.parentStorage.world, holder.getUuid()));
        }
    }

    public void clearTridentPosition(UUID tridentUuid) {
        this.ownedTridents.remove(tridentUuid);
    }

    @NotNull
    @Override
    public Iterator<TridentEntry> iterator() {
        return this.ownedTridents.values().iterator();
    }

    public boolean isEmpty() {
        return this.ownedTridents.isEmpty();
    }

    public void fromTag(CompoundTag ownerNbt) {
        ListTag tridentsNbt = ownerNbt.getList("tridents", NbtType.COMPOUND);
        for (int j = 0; j < tridentsNbt.size(); j++) {
            CompoundTag tridentNbt = tridentsNbt.getCompound(j);
            UUID tridentUuid = tridentNbt.method_25926("trident_uuid");
            this.ownedTridents.put(tridentUuid, TridentEntry.fromNbt(this.parentStorage.world, tridentNbt));
        }
    }

    public void toTag(CompoundTag ownerNbt) {
        ListTag tridentsNbt = new ListTag();
        this.ownedTridents.forEach((uuid, pos) -> {
            CompoundTag tridentNbt = pos.toNbt(new CompoundTag());
            tridentNbt.method_25927("trident_uuid", uuid);
            tridentsNbt.add(tridentNbt);
        });
        ownerNbt.put("tridents", tridentsNbt);
    }
}
