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
package io.github.ladysnake.sincereloyalty.mixin.client;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract CompoundTag getSubTag(String key);

    @Nullable
    @Unique
    private static String trueOwnerName;

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;appendEnchantments(Ljava/util/List;Lnet/minecraft/nbt/ListTag;)V"))
    private void captureThis(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        CompoundTag loyaltyNbt = this.getSubTag(LoyalTrident.MOD_NBT_KEY);
        if (loyaltyNbt != null && loyaltyNbt.contains(LoyalTrident.OWNER_NAME_NBT_KEY)) {
            trueOwnerName = loyaltyNbt.getString(LoyalTrident.OWNER_NAME_NBT_KEY);
        }
    }

    // inject into the lambda in appendEnchantments
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(method = "method_17869", at = @At("RETURN"))
    private static void editTooltip(List<Text> lines, CompoundTag enchantmentNbt, Enchantment enchantment, CallbackInfo info) {
        if (enchantment == Enchantments.LOYALTY && trueOwnerName != null) {
            if (!lines.isEmpty()) {
                lines.get(lines.size() - 1).append(new LiteralText(" ")).append(new TranslatableText("sincereloyalty.tooltip.owned_by", trueOwnerName).formatted(Formatting.DARK_GRAY));
            }
            trueOwnerName = null;
        }
    }
}
