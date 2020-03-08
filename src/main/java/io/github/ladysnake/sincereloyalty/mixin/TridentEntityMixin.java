package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.LoyalTrident;
import io.github.ladysnake.sincereloyalty.SincereLoyalty;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends ProjectileEntity implements LoyalTrident {
    @Shadow
    private ItemStack tridentStack;

    @Nullable
    @Unique
    private Boolean veryLoyalTrident;

    protected TridentEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void loyaltrident_sit() {
        LoyalTrident.addSittingFlag(this.tridentStack);
    }

    @Override
    public void loyaltrident_wakeUp() {
        LoyalTrident.clearSittingFlag(this.tridentStack);
    }

    @Override
    public void loyaltrident_setReturnSlot(int slot) {
        LoyalTrident.setPreferredSlot(this.tridentStack, slot);
    }

    /**
     * Calling {@code super.onEntityHit()} causes tridents to behave like arrows, disappearing on contact.
     * This removes the call, fixing the bug.
     */
    @Redirect(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V"))
    private void fixMojangStupid(ProjectileEntity projectileEntity, EntityHitResult entityHitResult) {
        // NO-OP
    }

    /**
     * If the trident was dropped as an item, we want it to stay in place and not immediately return to its owner.
     *
     * <p> This redirects the loyalty check, preventing the trident from going back after it hits something,
     * and preventing it from dropping if the owner dies.
     */
    @Redirect(
        method = "tick",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/data/DataTracker;get(Lnet/minecraft/entity/data/TrackedData;)Ljava/lang/Object;")
    )
    private Object sit(DataTracker self, TrackedData<Byte> trackedData) {
        if (this.isVeryLoyalTrident()) {
            // If your owner told you to sit, you sit
            if (LoyalTrident.hasSittingFlag(this.tridentStack)) {
                return (byte) 0;
            }
        }
        return self.get(trackedData);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tickTrident(CallbackInfo ci) {
        if (this.isVeryLoyalTrident()) {
            // Keep track of this trident's position at all time, in case the chunk goes unloaded
            SincereLoyalty.LOYAL_TRIDENTS.get(this.world).memorizeTrident(((ProjectileAccessor) this).getOwnerUuid(), ((TridentEntity) (Object) this));
        }
    }

    @Unique
    private boolean isVeryLoyalTrident() {
        if (this.veryLoyalTrident == null) {
            UUID trueOwner = LoyalTrident.getTrueOwner(this.tridentStack);
            this.veryLoyalTrident = trueOwner != null;
            // Not the owner == no loyalty
            if (this.veryLoyalTrident && !Objects.equals(trueOwner, ((ProjectileAccessor) this).getOwnerUuid())) {
                this.loyaltrident_sit();
            }
        }
        return this.veryLoyalTrident;
    }

    /**
     * Clears the sitting flag on the stack, so that loyalty tridents can still work when thrown after being retrieved.
     *
     * <p> Note that if the returned stack is immediately dropped, a new sitting trident will be created.
     */
    @Inject(method = "asItemStack", at = @At("RETURN"), cancellable = true)
    private void reenableLoyalty(CallbackInfoReturnable<ItemStack> cir) {
        LoyalTrident.clearSittingFlag(cir.getReturnValue());
    }
}