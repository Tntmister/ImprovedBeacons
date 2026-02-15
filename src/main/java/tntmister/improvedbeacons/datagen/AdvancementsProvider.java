package tntmister.improvedbeacons.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import tntmister.improvedbeacons.ImprovedBeacons;
import tntmister.improvedbeacons.advancements.MaxedBeaconCriterion;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class AdvancementsProvider extends FabricAdvancementProvider {

    protected AdvancementsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> consumer) {
        Advancement.Builder.advancement()
            .display(
                Items.BEACON,
                Component.literal("Overcompensator"),
                Component.literal("And ").append(Component.literal("this").withStyle(ChatFormatting.ITALIC)).append(" is to go even further beyond!"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                true
            ).parent(Advancement.Builder.advancement().build(Identifier.parse("minecraft:nether/create_full_beacon")))
            .addCriterion("maxed_beacon", tntmister.improvedbeacons.advancements.AdvancementCriteria.MAXED_BEACON.createCriterion(new MaxedBeaconCriterion.Conditions(Optional.empty(), 100)))
            .save(consumer, ImprovedBeacons.MOD_ID + ":maxed_beacon");
    }
}