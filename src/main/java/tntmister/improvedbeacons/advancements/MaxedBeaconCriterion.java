package tntmister.improvedbeacons.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MaxedBeaconCriterion extends SimpleCriterionTrigger<MaxedBeaconCriterion.Conditions> {

    @Override
    public @NotNull Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<ContextAwarePredicate> playerPredicate, int beaconPower) implements SimpleCriterionTrigger.SimpleInstance {
        public static Codec<Conditions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ContextAwarePredicate.CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                Codec.INT.fieldOf("beaconPower").forGetter(Conditions::beaconPower)
        ).apply(instance, Conditions::new));

        @Override
        public @NotNull Optional<ContextAwarePredicate> player() {
            return playerPredicate;
        }

        public boolean requirementsMet(int currentBeaconPower) {
            return currentBeaconPower == beaconPower;
        }
    }
    public void trigger(ServerPlayer player, int beaconPower) {
        trigger(player, conditions -> conditions.requirementsMet(beaconPower));
    }
}