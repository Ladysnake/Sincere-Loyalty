/*
 * Sincere Loyalty
 * Copyright (C) 2020 Ladysnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; If not, see <https://www.gnu.org/licenses>.
 */
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
