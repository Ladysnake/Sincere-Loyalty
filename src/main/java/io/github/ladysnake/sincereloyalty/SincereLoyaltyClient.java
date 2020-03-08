package io.github.ladysnake.sincereloyalty;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

public final class SincereLoyaltyClient implements ClientModInitializer {
    public static final SincereLoyaltyClient INSTANCE = new SincereLoyaltyClient();
    public static final int RECALL_ANIMATION_START = 10;
    public static final int RECALL_TIME = 40;

    private int useTime = 0;
    private int failedUseCountdown = 0;

    public void setFailedUse(int itemUseCooldown) {
        this.failedUseCountdown = itemUseCooldown + 1;
    }

    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register(mc -> {
            TridentRecaller.RecallStatus recalling = tickTridentRecalling(mc);
            if (recalling != null) {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeEnumConstant(recalling);
                ClientSidePacketRegistry.INSTANCE.sendToServer(SincereLoyalty.RECALL_TRIDENTS_MESSAGE_ID, buf);
            }
        });
        ClientSidePacketRegistry.INSTANCE.register(SincereLoyalty.RECALLING_MESSAGE_ID, (ctx, buf) -> {
            int playerId = buf.readInt();
            TridentRecaller.RecallStatus recalling = buf.readEnumConstant(TridentRecaller.RecallStatus.class);
            ctx.getTaskQueue().execute(() -> {
                Entity player = ctx.getPlayer().world.getEntityById(playerId);
                if (player instanceof TridentRecaller) {
                    ((TridentRecaller) player).sincereloyalty_updateRecallStatus(recalling);
                }
            });
        });
    }

    @Nullable
    private TridentRecaller.RecallStatus tickTridentRecalling(MinecraftClient mc) {
        if (this.failedUseCountdown > 0) {
            PlayerEntity player = mc.player;

            if (player != null && player.getMainHandStack().isEmpty()) {
                ++this.useTime;

                if (this.useTime == RECALL_ANIMATION_START) {
                    return TridentRecaller.RecallStatus.CHARGING;
                } else if (this.useTime == RECALL_TIME) {
                    this.useTime = 0;
                    return TridentRecaller.RecallStatus.RECALL;
                }
            }
            this.failedUseCountdown--;
        } else if (this.useTime > 0) {
            this.useTime = 0;
            return TridentRecaller.RecallStatus.CANCEL;
        }
        return null;
    }
}
