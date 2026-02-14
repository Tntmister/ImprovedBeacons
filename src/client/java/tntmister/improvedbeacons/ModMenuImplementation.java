package tntmister.improvedbeacons;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;
import tntmister.improvedbeacons.config.ImprovedBeaconsConfig;

public class ModMenuImplementation implements ModMenuApi {
    public ImprovedBeaconsConfig config = ImprovedBeaconsConfig.HANDLER.instance();

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parentScreen -> YetAnotherConfigLib.createBuilder()
                .title(Component.literal("Improved Beacons Config"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Improved Beacons"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Restrict Payment to at or above majority block tier."))
                                .description(OptionDescription.of(Component.literal("Example: A beacon with a pyramid made up of more Emerald Blocks than any other block will only allow payment using Emeralds of Netherite.")))
                                .binding(true, () -> config.restrictBeaconPaymentByMajorityBlock, newVal -> config.restrictBeaconPaymentByMajorityBlock = newVal)
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .save(ImprovedBeaconsConfig.HANDLER::save)
                .build()
                .generateScreen(parentScreen);
    }
}
