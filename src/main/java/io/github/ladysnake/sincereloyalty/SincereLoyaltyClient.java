package io.github.ladysnake.sincereloyalty;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.PacketByteBuf;

public final class SincereLoyaltyClient implements ClientModInitializer {
    public static final SincereLoyaltyClient INSTANCE = new SincereLoyaltyClient();

    private int useTime = 0;
    private int failedUseCountdown = 0;

    public boolean isRecallingTrident() {
        return this.useTime > 10;
    }

    public void setFailedUse(int itemUseCooldown) {
        this.failedUseCountdown = itemUseCooldown + 1;
    }

    @Override
    public void onInitializeClient() {
        ClientTickCallback.EVENT.register(mc -> {
            if (this.failedUseCountdown > 0) {
                PlayerEntity player = mc.player;
                if (player != null && player.getMainHandStack().isEmpty()) {
                    if (SincereLoyalty.LOYAL_TRIDENTS.get(player.world).hasTridents(player)) {
                        if (++this.useTime > 40) {
                            ClientSidePacketRegistry.INSTANCE.sendToServer(SincereLoyalty.RECALL_TRIDENTS_MESSAGE_ID, new PacketByteBuf(Unpooled.buffer()));
                            this.useTime = 0;
                        }
                    }
                }
                this.failedUseCountdown--;
            } else {
                this.useTime = 0;
            }
        });
    }
}
