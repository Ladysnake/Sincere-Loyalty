package io.github.ladysnake.sincereloyalty.mixin;

import io.github.ladysnake.sincereloyalty.TridentRecaller;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements TridentRecaller {
    @Unique
    private RecallStatus recallingTrident;

    @Override
    public boolean isRecallingTrident() {
        return recallingTrident == TridentRecaller.RecallStatus.CHARGING;
    }

    @Override
    public boolean sincereloyalty_updateRecallStatus(RecallStatus recallingTrident) {
        if (this.recallingTrident != recallingTrident) {
            this.recallingTrident = recallingTrident;
            return true;
        }
        return false;
    }
}
