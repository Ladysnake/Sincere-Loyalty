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
