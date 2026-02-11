package tntmister.improvedbeacons.advancements;

import net.minecraft.advancements.CriteriaTriggers;
import tntmister.improvedbeacons.ImprovedBeacons;

public class AdvancementCriteria {
    public static final MaxedBeaconCriterion MAXED_BEACON = CriteriaTriggers.register(ImprovedBeacons.MOD_ID + ":maxed_beacon", new MaxedBeaconCriterion());

    public static void init(){}
}