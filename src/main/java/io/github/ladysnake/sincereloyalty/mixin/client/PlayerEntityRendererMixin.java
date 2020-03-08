package io.github.ladysnake.sincereloyalty.mixin.client;

import io.github.ladysnake.sincereloyalty.TridentRecaller;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @ModifyVariable(method = "getArmPose", at = @At(value = "RETURN", shift = At.Shift.BEFORE))
    private BipedEntityModel.ArmPose getArmPose(BipedEntityModel.ArmPose initialPose, AbstractClientPlayerEntity player) {
        if (initialPose == BipedEntityModel.ArmPose.EMPTY && ((TridentRecaller) player).isRecallingTrident()) {
            return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
        }
        return initialPose;
    }
}
