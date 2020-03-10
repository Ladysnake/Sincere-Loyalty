package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @ModifyVariable(method = "onCreativeInventoryAction", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/packet/c2s/play/CreativeInventoryActionC2SPacket;getItemStack()Lnet/minecraft/item/ItemStack;"))
    private ItemStack removeTridentUuid(ItemStack copiedStack) {
        CompoundTag compoundTag = copiedStack.getSubTag(LoyalTrident.MOD_NBT_KEY);
        if (compoundTag != null) {
            compoundTag.remove(LoyalTrident.TRIDENT_UUID_NBT_KEY);  // prevent stupid copies of the exact same trident
        }
        return copiedStack;
    }
}
