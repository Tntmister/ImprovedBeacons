package tntmister.improvedbeacons.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

public class ImprovedBeaconsConfig {
    public static ConfigClassHandler<ImprovedBeaconsConfig> HANDLER = ConfigClassHandler.createBuilder(ImprovedBeaconsConfig.class)
            .id(ResourceLocation.fromNamespaceAndPath("improved-beacons", "improved-beacons"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("improved-beacons.json"))
                    .build())
            .build();

    @SerialEntry
    public boolean restrictBeaconPaymentByMajorityBlock = false;
}
