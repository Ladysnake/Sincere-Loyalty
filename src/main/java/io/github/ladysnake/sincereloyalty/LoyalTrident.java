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

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public interface LoyalTrident {
    String MOD_NBT_KEY = SincereLoyalty.MOD_ID;
    String TRIDENT_UUID_NBT_KEY = "trident_uuid";
    String OWNER_NAME_NBT_KEY = "owner_name";
    String TRIDENT_OWNER_NBT_KEY = "trident_owner";
    String TRIDENT_SIT_NBT_KEY = "trident_sit";
    String RETURN_SLOT_NBT_KEY = "return_slot";

    static LoyalTrident of(TridentEntity trident) {
        return ((LoyalTrident) trident);
    }

    static UUID getTridentUuid(ItemStack stack) {
        CompoundTag loyaltyData = stack.getOrCreateSubTag(LoyalTrident.MOD_NBT_KEY);
        if (!loyaltyData.method_25928(TRIDENT_UUID_NBT_KEY)) {
            loyaltyData.method_25927(LoyalTrident.TRIDENT_UUID_NBT_KEY, UUID.randomUUID());
        }
        return loyaltyData.method_25926(TRIDENT_UUID_NBT_KEY);
    }

    static void setPreferredSlot(ItemStack tridentStack, int slot) {
        tridentStack.getOrCreateSubTag(LoyalTrident.MOD_NBT_KEY).putInt(LoyalTrident.RETURN_SLOT_NBT_KEY, slot);
    }

    static boolean hasSittingFlag(ItemStack tridentStack) {
        CompoundTag loyaltyData = tridentStack.getSubTag(LoyalTrident.MOD_NBT_KEY);
        return loyaltyData != null && loyaltyData.getBoolean(LoyalTrident.TRIDENT_SIT_NBT_KEY);
    }

    static void addSittingFlag(ItemStack tridentStack) {
        tridentStack.getOrCreateSubTag(LoyalTrident.MOD_NBT_KEY).putBoolean(LoyalTrident.TRIDENT_SIT_NBT_KEY, true);
    }

    static void clearSittingFlag(ItemStack tridentStack) {
        CompoundTag loyaltyData = tridentStack.getSubTag(LoyalTrident.MOD_NBT_KEY);
        if (loyaltyData != null) {
            loyaltyData.remove(LoyalTrident.TRIDENT_SIT_NBT_KEY);
        }
    }

    static boolean hasTrueOwner(ItemStack tridentStack) {
        if (SincereLoyalty.TRIDENTS.contains(tridentStack.getItem()) && EnchantmentHelper.getLoyalty(tridentStack) > 0) {
            CompoundTag loyaltyNbt = tridentStack.getSubTag(MOD_NBT_KEY);
            return loyaltyNbt != null && loyaltyNbt.method_25928(TRIDENT_OWNER_NBT_KEY);
        }
        return false;
    }

    @Nullable
    static UUID getTrueOwner(ItemStack tridentStack) {
        return hasTrueOwner(tridentStack) ? Objects.requireNonNull(tridentStack.getSubTag(MOD_NBT_KEY)).method_25926(TRIDENT_OWNER_NBT_KEY) : null;
    }

    @Nullable
    static TridentEntity spawnTridentForStack(Entity thrower, ItemStack tridentStack) {
        CompoundTag loyaltyData = tridentStack.getSubTag(MOD_NBT_KEY);
        if (loyaltyData != null) {
            UUID ownerUuid = loyaltyData.method_25926(TRIDENT_OWNER_NBT_KEY);
            if (ownerUuid != null) {
                PlayerEntity owner = thrower.world.getPlayerByUuid(ownerUuid);
                if (owner != null) {
                    TridentEntity trident = new TridentEntity(thrower.world, owner, tridentStack);
                    trident.setVelocity(thrower.getVelocity());
                    trident.copyPositionAndRotation(thrower);
                    thrower.world.spawnEntity(trident);
                    LoyalTrident.of(trident).loyaltrident_sit();
                    return trident;
                }
            }
        }
        return null;
    }

    UUID loyaltrident_getTridentUuid();

    void loyaltrident_sit();

    void loyaltrident_wakeUp();

    void loyaltrident_setReturnSlot(int slot);
}
