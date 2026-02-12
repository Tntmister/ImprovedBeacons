package tntmister.improvedbeacons.mixin.client;

import net.minecraft.world.inventory.BeaconMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import tntmister.improvedbeacons.ImprovedBeacons;

@Mixin(BeaconMenu.class)
public class BeaconMenuMixin {

    @ModifyArg(method = "<init>(ILnet/minecraft/world/Container;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/SimpleContainerData;<init>(I)V"))
    private static int BeaconMenu(int size){
        return size + ImprovedBeacons.DATA_NUM_VALUES;
    }
}
