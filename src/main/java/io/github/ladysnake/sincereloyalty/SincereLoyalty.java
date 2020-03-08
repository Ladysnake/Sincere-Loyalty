package io.github.ladysnake.sincereloyalty;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;

import java.util.stream.Stream;

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
            TridentRecaller.RecallStatus charging = buf.readEnumConstant(TridentRecaller.RecallStatus.class);

            ctx.getTaskQueue().execute(() -> {
                PlayerEntity player = ctx.getPlayer();
                LoyalTridentStorage loyalTridentStorage = LoyalTridentStorage.get((ServerWorld) player.world);
                TridentRecaller.RecallStatus actualRecallStatus = loyalTridentStorage.hasTridents(player) ? charging : TridentRecaller.RecallStatus.CANCEL;

                if (((TridentRecaller) player).sincereloyalty_updateRecallStatus(actualRecallStatus)) {
                    if (actualRecallStatus == TridentRecaller.RecallStatus.RECALL) {
                        loyalTridentStorage.recallTridents(player);
                    }
                    PacketByteBuf res = new PacketByteBuf(Unpooled.buffer());
                    res.writeInt(player.getEntityId());
                    res.writeEnumConstant(actualRecallStatus);
                    Stream.concat(Stream.of(player), PlayerStream.watching(player))
                        .forEach(p -> ServerSidePacketRegistry.INSTANCE.sendToPlayer(p, RECALLING_MESSAGE_ID, res));
                }
            });
        });
    }
}
