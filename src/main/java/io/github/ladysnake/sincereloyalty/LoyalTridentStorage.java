package io.github.ladysnake.sincereloyalty;

import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class LoyalTridentStorage extends PersistentState {

    public static LoyalTridentStorage get(ServerWorld world) {
        final String id = SincereLoyalty.MOD_ID + "_trident_storage";
        return world.getPersistentStateManager().getOrCreate(() -> new LoyalTridentStorage(id, world), id);
    }

    /** Player UUID -> Trident UUID -> Trident Position */
    private final Map<UUID, OwnedTridents> tridents = new HashMap<>();
    final ServerWorld world;

    public LoyalTridentStorage(String id, ServerWorld world) {
        super(id);
        this.world = world;
    }

    public boolean hasTridents(PlayerEntity player) {
        return !this.tridents.getOrDefault(player.getUuid(), OwnedTridents.EMPTY).isEmpty();
    }

    public void memorizeTrident(UUID owner, TridentEntity trident) {
        BlockPos tridentPos = trident.getSenseCenterPos();
        this.tridents.computeIfAbsent(owner, o -> new OwnedTridents(this)).storeTridentPosition(trident.getUuid(), tridentPos);
    }

    public void forgetTrident(UUID owner, TridentEntity trident) {
        this.tridents.getOrDefault(owner, OwnedTridents.EMPTY).clearTridentPosition(trident.getUuid());
    }

    public void recallTridents(PlayerEntity player) {
        for (TridentEntity trident : this.tridents.getOrDefault(player.getUuid(), OwnedTridents.EMPTY)) {
            ((LoyalTrident) trident).loyaltrident_wakeUp();

            if (trident.distanceTo(player) > 64) {
                // reposition the trident at the same angle to the player but only 64 blocks away
                Vec3d newPos = player.getPos().add(trident.getPos().subtract(player.getPos()).normalize().multiply(64));
                trident.resetPosition(newPos.x, newPos.y, newPos.z);
            }

            ((LoyalTrident) trident).loyaltrident_setReturnSlot(player.inventory.selectedSlot);
            this.world.playSound(player, trident.getX(), trident.getY(), trident.getZ(), SoundEvents.ITEM_TRIDENT_RETURN, trident.getSoundCategory(), 2.0f, 0.7f);
            ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, new PlaySoundIdS2CPacket(SoundEvents.ITEM_TRIDENT_RETURN.getId(), trident.getSoundCategory(), trident.getPos(), trident.distanceTo(player) / 8, 0.7f));
        }
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.tridents.clear();
        ListTag ownersNbt = tag.getList("trident_owners", NbtType.COMPOUND);
        for (int i = 0; i < ownersNbt.size(); i++) {
            OwnedTridents tridents = new OwnedTridents(this);
            CompoundTag ownerNbt = ownersNbt.getCompound(i);
            UUID ownerUuid = ownerNbt.method_25926("owner_uuid");
            tridents.fromTag(ownerNbt);
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
                    ownerNbt.method_25927("owner_uuid", ownerUuid);
                    tridents.toTag(ownerNbt);
                    ownersNbt.add(ownerNbt);
                }
            });
            tag.put("trident_owners", ownersNbt);
        }
        return tag;
    }

}
