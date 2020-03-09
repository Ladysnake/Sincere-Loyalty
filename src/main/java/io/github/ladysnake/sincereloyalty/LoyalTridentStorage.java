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
package io.github.ladysnake.sincereloyalty;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class LoyalTridentStorage extends PersistentState {

    public static LoyalTridentStorage get(ServerWorld world) {
        final String id = SincereLoyalty.MOD_ID + "_trident_storage";
        return world.getPersistentStateManager().getOrCreate(() -> new LoyalTridentStorage(id, world), id);
    }

    /** Player UUID -> Trident UUID -> Trident Position */
    private final Map<UUID, OwnedTridents> tridents = new HashMap<>();
    final ServerWorld world;

    public LoyalTridentStorage(String id, ServerWorld world) {
        super(id);
        this.world = world;
    }

    public boolean hasTridents(PlayerEntity player) {
        return !this.tridents.getOrDefault(player.getUuid(), OwnedTridents.EMPTY).isEmpty();
    }

    public void memorizeTrident(UUID owner, TridentEntity trident) {
        BlockPos tridentPos = trident.getSenseCenterPos();
        this.tridents.computeIfAbsent(owner, o -> new OwnedTridents(this)).storeTridentPosition(trident.getUuid(), tridentPos);
    }

    public void forgetTrident(UUID owner, TridentEntity trident) {
        this.tridents.getOrDefault(owner, OwnedTridents.EMPTY).clearTridentPosition(trident.getUuid());
    }

    /**
     * @return {@code true} if at least one trident was recalled
     */
    public boolean recallTridents(PlayerEntity player) {
        boolean foundAny = false;
        for (TridentEntity trident : this.tridents.getOrDefault(player.getUuid(), OwnedTridents.EMPTY)) {
            float initialDistance = trident.distanceTo(player);
            ((LoyalTrident) trident).loyaltrident_wakeUp();

            if (initialDistance > 64) {
                // reposition the trident at the same angle to the player but only 64 blocks away
                Vec3d newPos = player.getPos().add(trident.getPos().subtract(player.getPos()).normalize().multiply(64));
                trident.resetPosition(newPos.x, newPos.y, newPos.z);
            }

            ((LoyalTrident) trident).loyaltrident_setReturnSlot(player.inventory.selectedSlot);
            this.world.playSound(player, trident.getX(), trident.getY(), trident.getZ(), SoundEvents.ITEM_TRIDENT_RETURN, trident.getSoundCategory(), 2.0f, 0.7f);
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, new PlaySoundIdS2CPacket(SoundEvents.ITEM_TRIDENT_RETURN.getId(), trident.getSoundCategory(), trident.getPos(), trident.distanceTo(player) / 8, 0.7f));
            foundAny = true;
        }
        return foundAny;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.tridents.clear();
        ListTag ownersNbt = tag.getList("trident_owners", NbtType.COMPOUND);
        for (int i = 0; i < ownersNbt.size(); i++) {
            OwnedTridents tridents = new OwnedTridents(this);
            CompoundTag ownerNbt = ownersNbt.getCompound(i);
            UUID ownerUuid = ownerNbt.method_25926("owner_uuid");
            tridents.fromTag(ownerNbt);
            this.tridents.put(ownerUuid, tridents);
        }
    }

    @NotNull
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (!this.tridents.isEmpty()) {
            ListTag ownersNbt = new ListTag();
            this.tridents.forEach((ownerUuid, tridents) -> {
                if (!tridents.isEmpty()) {
                    CompoundTag ownerNbt = new CompoundTag();
                    ownerNbt.method_25927("owner_uuid", ownerUuid);
                    tridents.toTag(ownerNbt);
                    ownersNbt.add(ownerNbt);
                }
            });
            tag.put("trident_owners", ownersNbt);
        }
        return tag;
    }

}
