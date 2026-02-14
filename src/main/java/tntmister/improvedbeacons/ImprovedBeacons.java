package tntmister.improvedbeacons;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tntmister.improvedbeacons.advancements.AdvancementCriteria;
import tntmister.improvedbeacons.config.ImprovedBeaconsConfig;

public class ImprovedBeacons implements ModInitializer {
	public static final String MOD_ID = "improved-beacons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// number of entries added to the BeaconBlockEntity ContainerData (can't add public static to mixin)
	public static final int DATA_NUM_VALUES = 2;

	@Override
	public void onInitialize() {
		AdvancementCriteria.init();
		ImprovedBeaconsConfig.HANDLER.load();
		LOGGER.info("Improved Beacons initialised!");
	}
}