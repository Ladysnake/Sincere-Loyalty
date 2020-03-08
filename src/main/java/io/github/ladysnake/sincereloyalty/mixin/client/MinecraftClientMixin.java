package io.github.ladysnake.sincereloyalty.mixin.client;

import io.github.ladysnake.sincereloyalty.SincereLoyaltyClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    private int itemUseCooldown;

    @Inject(method = "doItemUse", at = @At("TAIL"))
    private void onUseFail(CallbackInfo ci) {
        SincereLoyaltyClient.INSTANCE.setFailedUse(this.itemUseCooldown);
    }
}
