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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
    @ModifyVariable(method = "onStoppedUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/TridentEntity;setProperties(Lnet/minecraft/entity/Entity;FFFFF)V"))
    private TridentEntity setTridentReturnSlot(TridentEntity trident, ItemStack stack, World world, LivingEntity user) {
        LoyalTrident.of(trident).loyaltrident_setReturnSlot(((PlayerEntity) user).inventory.selectedSlot);
        return trident;
    }
}
