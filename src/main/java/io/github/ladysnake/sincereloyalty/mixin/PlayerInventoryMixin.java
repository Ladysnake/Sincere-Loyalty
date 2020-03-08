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
package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {

    @Shadow
    public abstract ItemStack getInvStack(int slot);

    @ModifyArg(method = "insertStack(Lnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(ILnet/minecraft/item/ItemStack;)Z"))
    private int insertToPreferredSlot(int slot, ItemStack stack) {
        CompoundTag tag = stack.getSubTag(LoyalTrident.MOD_NBT_KEY);
        if (tag != null && tag.contains(LoyalTrident.RETURN_SLOT_NBT_KEY)) {
            int preferredSlot = tag.getInt(LoyalTrident.RETURN_SLOT_NBT_KEY);
            if (this.getInvStack(preferredSlot).isEmpty()) {
                return preferredSlot;
            }
        }
        return slot;
    }
}
