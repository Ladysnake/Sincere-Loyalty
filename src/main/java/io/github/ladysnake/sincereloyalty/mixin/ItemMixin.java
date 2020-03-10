/*
 * Sincere-Loyalty
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
import io.github.ladysnake.sincereloyalty.storage.LoyalTridentStorage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

@Mixin(Item.class)
public abstract class ItemMixin {
    @Inject(method = "inventoryTick", at = @At("RETURN"))
    private void updateTridentInInventory(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (entity.age % 10 == 0 && !entity.world.isClient && entity instanceof PlayerEntity) {
            UUID trueOwner = LoyalTrident.getTrueOwner(stack);
            if (Objects.equals(trueOwner, entity.getUuid())) {
                CompoundTag loyaltyData = Objects.requireNonNull(stack.getSubTag(LoyalTrident.MOD_NBT_KEY));
                if (!Objects.equals(entity.getEntityName(), loyaltyData.getString(LoyalTrident.OWNER_NAME_NBT_KEY))) {
                    loyaltyData.putString(LoyalTrident.OWNER_NAME_NBT_KEY, entity.getEntityName());
                }
            } else {
                LoyalTridentStorage.get((ServerWorld) world).memorizeTrident(trueOwner, LoyalTrident.getTridentUuid(stack), (PlayerEntity) entity);
            }
        }
    }
}
