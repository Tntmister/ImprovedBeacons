package tntmister.improvedbeacons;

import net.fabricmc.api.ClientModInitializer;
import tntmister.improvedbeacons.advancements.AdvancementCriteria;

public class ImprovedBeaconsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AdvancementCriteria.init();
	}
}