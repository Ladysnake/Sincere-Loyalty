package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
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
    private void updateName(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (entity.age % 10 == 0) {
            UUID trueOwner = LoyalTrident.getTrueOwner(stack);
            if (Objects.equals(trueOwner, entity.getUuid())) {
                CompoundTag loyaltyData = Objects.requireNonNull(stack.getSubTag(LoyalTrident.MOD_NBT_KEY));
                if (!Objects.equals(entity.getEntityName(), loyaltyData.getString(LoyalTrident.OWNER_NAME_NBT_KEY))) {
                    loyaltyData.putString(LoyalTrident.OWNER_NAME_NBT_KEY, entity.getEntityName());
                }
            }
        }
    }
}
