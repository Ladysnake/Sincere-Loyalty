package io.github.ladysnake.sincereloyalty;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public interface LoyalTrident {
    String MOD_NBT_KEY = SincereLoyalty.MOD_ID;
    String TRIDENT_OWNER_NBT_KEY = "trident_owner";
    String TRIDENT_SIT_NBT_KEY = "trident_sit";
    String RETURN_SLOT_NBT_KEY = "return_slot";

    static LoyalTrident of(TridentEntity trident) {
        return ((LoyalTrident) trident);
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

    void loyaltrident_sit();

    void loyaltrident_wakeUp();

    void loyaltrident_setReturnSlot(int slot);
}
