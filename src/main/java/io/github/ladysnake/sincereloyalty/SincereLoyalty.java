package io.github.ladysnake.sincereloyalty;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.WorldComponentCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class SincereLoyalty implements ModInitializer {
    public static final ComponentType<LoyalTridentStorage> LOYAL_TRIDENTS =
        ComponentRegistry.INSTANCE.registerIfAbsent(id("loyal_tridents"), LoyalTridentStorage.class)
            .attach(WorldComponentCallback.EVENT, LoyalTridentStorage::new);

    public static final String MOD_ID = "sincere-loyalty";

    public static final Tag<Item> LOYALTY_CATALYSTS = TagRegistry.item(id("loyalty_catalysts"));
    public static final Tag<Item> TRIDENTS = TagRegistry.item(id("tridents"));

    public static final Identifier RECALL_TRIDENTS_MESSAGE_ID = id("recall_tridents");

    public static Identifier id(String path) {
        return new Identifier("sincere-loyalty", path);
    }

    @Override
    public void onInitialize() {
        ServerSidePacketRegistry.INSTANCE.register(RECALL_TRIDENTS_MESSAGE_ID, (ctx, b) -> ctx.getTaskQueue().execute(() -> {
            PlayerEntity player = ctx.getPlayer();
            LoyalTridentStorage loyalTridentStorage = LOYAL_TRIDENTS.get(player.world);

            if (loyalTridentStorage.hasTridents(player)) {
                loyalTridentStorage.recallTridents(player);
            }
        }));
    }
}
