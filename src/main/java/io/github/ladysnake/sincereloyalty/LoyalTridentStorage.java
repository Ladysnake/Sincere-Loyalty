package io.github.ladysnake.sincereloyalty;

import io.netty.buffer.Unpooled;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.Component;
import nerdhub.cardinal.components.api.util.sync.WorldSyncedComponent;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LoyalTridentStorage implements Component, WorldSyncedComponent {
    private final World world;
    /**
     * Player UUID -> Trident UUID -> Trident Position
     */
    private final Map<UUID, Map<UUID, BlockPos>> tridents = new HashMap<>();

    public LoyalTridentStorage(World world) {
        this.world = world;
    }

    public boolean hasTridents(PlayerEntity player) {
        return !this.tridents.getOrDefault(player.getUuid(), Collections.emptyMap()).isEmpty();
    }

    public void memorizeTrident(UUID owner, TridentEntity trident) {
        if (!this.world.isClient) {
            BlockPos tridentPos = trident.getSenseCenterPos();
            if (!tridentPos.equals(this.tridents.computeIfAbsent(owner, o -> new HashMap<>()).put(trident.getUuid(), tridentPos))) {
                this.sync();
            }
        }
    }

    public void recallTridents(PlayerEntity player) {
        if (!this.world.isClient) {
            for (
                Iterator<Map.Entry<UUID, BlockPos>> iterator = this.tridents.get(player.getUuid()).entrySet().iterator();
                iterator.hasNext();
            ) {
                Map.Entry<UUID, BlockPos> entry = iterator.next();
                UUID uuid = entry.getKey();
                BlockPos pos = entry.getValue();
                // preload the chunk
                this.world.getChunk(pos);
                Entity trident = ((ServerWorld) this.world).getEntity(uuid);
                if (trident instanceof LoyalTrident) {
                    ((LoyalTrident) trident).loyaltrident_wakeUp();

                    if (trident.distanceTo(player) > 64) {
                        // reposition the trident at the same angle to the player but only 64 blocks away
                        Vec3d newPos = player.getPos().add(trident.getPos().subtract(player.getPos()).normalize().multiply(64));
                        trident.resetPosition(newPos.x, newPos.y, newPos.z);
                    }

                    ((LoyalTrident) trident).loyaltrident_setReturnSlot(player.inventory.selectedSlot);
                    this.world.playSound(player, trident.getX(), trident.getY(), trident.getZ(), SoundEvents.ITEM_TRIDENT_RETURN, trident.getSoundCategory(), 2.0f, 0.7f);
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, new PlaySoundIdS2CPacket(SoundEvents.ITEM_TRIDENT_RETURN.getId(), trident.getSoundCategory(), trident.getPos(), trident.distanceTo(player) / 8, 0.7f));
                } else {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void syncWith(ServerPlayerEntity player) {
        Map<UUID, BlockPos> playerTridents = this.tridents.get(player.getUuid());
        if (playerTridents != null && !playerTridents.isEmpty()) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeIdentifier(this.getComponentType().getId());
            buf.writeInt(playerTridents.size());
            playerTridents.forEach((uuid, blockPos) -> {
                buf.writeUuid(uuid);
                buf.writeBlockPos(blockPos);
            });
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, PACKET_ID, buf);
        }
    }

    @Override
    public void processPacket(PacketContext ctx, PacketByteBuf buf) {
        Map<UUID, BlockPos> playerTridents = new HashMap<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            playerTridents.put(buf.readUuid(), buf.readBlockPos());
        }
        this.tridents.put(ctx.getPlayer().getUuid(), playerTridents);
    }

    @Override
    public World getWorld() {
        return this.world;
    }

    @NotNull
    @Override
    public ComponentType<?> getComponentType() {
        return SincereLoyalty.LOYAL_TRIDENTS;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.tridents.clear();
        ListTag ownersNbt = tag.getList("trident_owners", NbtType.COMPOUND);
        for (int i = 0; i < ownersNbt.size(); i++) {
            Map<UUID, BlockPos> tridents = new HashMap<>();
            CompoundTag ownerNbt = ownersNbt.getCompound(i);
            UUID ownerUuid = ownerNbt.method_25926("owner_uuid");
            ListTag tridentsNbt = ownerNbt.getList("tridents", NbtType.COMPOUND);
            for (int j = 0; j < tridentsNbt.size(); j++) {
                CompoundTag tridentNbt = tridentsNbt.getCompound(j);
                UUID tridentUuid = tridentNbt.method_25926("trident_uuid");
                tridents.put(tridentUuid, NbtHelper.toBlockPos(tridentNbt));
            }
            this.tridents.put(ownerUuid, tridents);
        }
    }

    @NotNull
    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (!this.tridents.isEmpty()) {
            ListTag ownersNbt = new ListTag();
            this.tridents.forEach((ownerUuid, tridents) -> {
                if (!tridents.isEmpty()) {
                    CompoundTag ownerNbt = new CompoundTag();
                    ListTag tridentsNbt = new ListTag();
                    tridents.forEach((uuid, pos) -> {
                        CompoundTag tridentNbt = NbtHelper.fromBlockPos(pos);
                        tridentNbt.method_25927("trident_uuid", uuid);
                        tridentsNbt.add(tridentNbt);
                    });
                    ownerNbt.put("tridents", tridentsNbt);
                    ownerNbt.method_25927("owner_uuid", ownerUuid);
                }
            });
            tag.put("trident_owners", ownersNbt);
        }
        return tag;
    }
}
