package io.github.ladysnake.sincereloyalty.storage;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class InventoryTridentEntry extends TridentEntry {
    private final UUID playerUuid;

    InventoryTridentEntry(ServerWorld world, UUID tridentUuid, UUID playerUuid) {
        super(world, tridentUuid);
        this.playerUuid = playerUuid;
    }

    InventoryTridentEntry(ServerWorld world, CompoundTag tag) {
        super(world, tag);
        this.playerUuid = tag.getUuidNew("player_uuid");
    }

    @Override
    public CompoundTag toNbt(CompoundTag nbt) {
        super.toNbt(nbt);
        nbt.putUuidNew("player_uuid", this.playerUuid);
        return nbt;
    }

    @Override
    public TridentEntity findTrident() {
        PlayerEntity player = this.world.getPlayerByUuid(this.playerUuid);
        if (player != null) {
            for (int slot = 0; slot < player.inventory.getInvSize(); slot++) {
                ItemStack stack = player.inventory.getInvStack(slot);
                CompoundTag loyaltyData = stack.getSubTag(LoyalTrident.MOD_NBT_KEY);
                if (loyaltyData != null && loyaltyData.containsUuidNew(LoyalTrident.TRIDENT_UUID_NBT_KEY)) {
                    if (loyaltyData.getUuidNew(LoyalTrident.TRIDENT_UUID_NBT_KEY).equals(this.tridentUuid)) {
                        TridentEntity tridentEntity = LoyalTrident.spawnTridentForStack(player, stack);
                        if (tridentEntity != null) {
                            player.inventory.removeInvStack(slot);
                            return tridentEntity;
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean isHolder(PlayerEntity holder) {
        return this.playerUuid.equals(holder.getUuid());
    }
}
