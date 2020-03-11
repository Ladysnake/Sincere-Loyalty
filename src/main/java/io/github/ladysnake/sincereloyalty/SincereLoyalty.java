/*
 * Sincere-Loyalty
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
package io.github.ladysnake.sincereloyalty;

import io.github.ladysnake.sincereloyalty.storage.LoyalTridentStorage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public final class SincereLoyalty implements ModInitializer {

    public static final String MOD_ID = "sincere-loyalty";

    public static final Tag<Item> LOYALTY_CATALYSTS = TagRegistry.item(id("loyalty_catalysts"));
    public static final Tag<Item> TRIDENTS = TagRegistry.item(id("tridents"));

    public static final Identifier RECALL_TRIDENTS_MESSAGE_ID = id("recall_tridents");
    public static final Identifier RECALLING_MESSAGE_ID = id("recalling_tridents");

    public static Identifier id(String path) {
        return new Identifier("sincere-loyalty", path);
    }

    @Override
    public void onInitialize() {
        ServerSidePacketRegistry.INSTANCE.register(RECALL_TRIDENTS_MESSAGE_ID, (ctx, buf) -> {
            TridentRecaller.RecallStatus requested = buf.readEnumConstant(TridentRecaller.RecallStatus.class);

            ctx.getTaskQueue().execute(() -> {
                PlayerEntity player = ctx.getPlayer();
                LoyalTridentStorage loyalTridentStorage = LoyalTridentStorage.get((ServerWorld) player.world);
                TridentRecaller.RecallStatus currentRecallStatus = ((TridentRecaller) player).getCurrentRecallStatus();
                TridentRecaller.RecallStatus newRecallStatus;

                if (loyalTridentStorage.hasTridents(player)) {
                    if (currentRecallStatus != requested && requested == TridentRecaller.RecallStatus.RECALLING) {
                        // if there is no trident to recall, reset the player's animation
                        if (loyalTridentStorage.recallTridents(player)) {
                            newRecallStatus = TridentRecaller.RecallStatus.RECALLING;
                        } else {
                            player.addMessage(new TranslatableText("sincere-loyalty:trident_recall_fail"), true);
                            newRecallStatus = TridentRecaller.RecallStatus.NONE;
                        }
                    } else {
                        newRecallStatus = requested;
                    }
                } else {
                    newRecallStatus = TridentRecaller.RecallStatus.NONE;
                }

                ((TridentRecaller) player).updateRecallStatus(newRecallStatus);
            });
        });
    }
}
