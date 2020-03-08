package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract ItemStack getStack();

    @Shadow
    public abstract boolean cannotPickup();

    @Nullable
    @Unique
    private Boolean veryLoyalTrident;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tickItem(CallbackInfo ci) {
        // Only spawn a trident if it has a pickup delay (usually sign of it being dropped by a player)
        if (!this.world.isClient && this.cannotPickup()) {
            if (this.veryLoyalTrident == null) {
                this.veryLoyalTrident = LoyalTrident.hasTrueOwner(this.getStack());
            }
            if (this.veryLoyalTrident) {
                ItemStack tridentStack = this.getStack();
                CompoundTag loyaltyData = tridentStack.getSubTag(LoyalTrident.MOD_NBT_KEY);
                if (loyaltyData != null) {
                    UUID ownerUuid = loyaltyData.method_25926(LoyalTrident.TRIDENT_OWNER_NBT_KEY);
                    if (ownerUuid != null) {
                        PlayerEntity owner = this.world.getPlayerByUuid(ownerUuid);
                        if (owner != null) {
                            TridentEntity trident = new TridentEntity(this.world, owner, tridentStack);
                            LoyalTrident.of(trident).loyaltrident_sit();
                            trident.setVelocity(this.getVelocity());
                            this.world.spawnEntity(trident);
                            this.remove();
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}
