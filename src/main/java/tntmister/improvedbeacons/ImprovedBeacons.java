package tntmister.improvedbeacons;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImprovedBeacons implements ModInitializer {
	public static final String MOD_ID = "improved-beacons";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Improved Beacons initialised!");
	}
}