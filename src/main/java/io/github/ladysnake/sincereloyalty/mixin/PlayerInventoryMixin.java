package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
