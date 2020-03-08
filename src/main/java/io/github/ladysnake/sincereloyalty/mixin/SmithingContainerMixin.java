package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import io.github.ladysnake.sincereloyalty.SincereLoyalty;
import net.minecraft.class_4861;
import net.minecraft.class_4862;
import net.minecraft.container.BlockContext;
import net.minecraft.container.ContainerType;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(class_4862.class)
public abstract class SmithingContainerMixin extends class_4861 {
    public SmithingContainerMixin(ContainerType<?> containerType, int i, PlayerInventory playerInventory, BlockContext blockContext) {
        super(containerType, i, playerInventory, blockContext);
    }

    @Inject(method = "method_24927", at = @At("RETURN"), cancellable = true)
    private void canTakeResult(PlayerEntity playerEntity, boolean resultNonEmpty, CallbackInfoReturnable<Boolean> cir) {
        if (resultNonEmpty && !cir.getReturnValueZ()) {
            ItemStack item = this.field_22480.getInvStack(0);
            ItemStack upgradeItem = this.field_22480.getInvStack(1);
            cir.setReturnValue(SincereLoyalty.TRIDENTS.contains(item.getItem()) && SincereLoyalty.LOYALTY_CATALYSTS.contains(upgradeItem.getItem()));
        }
    }

    @ModifyArg(
        method = "method_24928",
        slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/item/ItemStack;EMPTY:Lnet/minecraft/item/ItemStack;")),
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/inventory/Inventory;setInvStack(ILnet/minecraft/item/ItemStack;)V"
        )
    )
    private ItemStack updateResult(ItemStack initialResult) {
        if (initialResult.isEmpty()) {
            ItemStack item = this.field_22480.getInvStack(0);
            ItemStack upgradeItem = this.field_22480.getInvStack(1);
            if (SincereLoyalty.TRIDENTS.contains(item.getItem()) && SincereLoyalty.LOYALTY_CATALYSTS.contains(upgradeItem.getItem())) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(item);
                if (enchantments.getOrDefault(Enchantments.LOYALTY, 0) == Enchantments.LOYALTY.getMaximumLevel()) {
                    ItemStack result = item.copy();
                    // we can mutate the map as it is recreated with every call to getEnchantments
                    enchantments.put(Enchantments.LOYALTY, Enchantments.LOYALTY.getMaximumLevel() + 1);
                    EnchantmentHelper.set(enchantments, result);
                    result.getOrCreateSubTag(LoyalTrident.MOD_NBT_KEY).method_25927(LoyalTrident.TRIDENT_OWNER_NBT_KEY, this.field_22482.getUuid());
                    return result;
                }
            }
        }
        return initialResult;
    }
}