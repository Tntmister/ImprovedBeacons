package tntmister.improvedbeacons.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.BeaconScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(BeaconScreen.class)
public abstract class BeaconScreenMixin extends AbstractContainerScreen<BeaconMenu> {
    public BeaconScreenMixin(BeaconMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Shadow
    @Final
    private List<BeaconScreen.BeaconButton> beaconButtons;

    @Shadow
    private <T extends AbstractWidget & BeaconScreen.BeaconButton> void addBeaconButton(T beaconButton) {}

    @Unique
    private static final Identifier NEW_BEACON_LOCATION = Identifier.fromNamespaceAndPath("improved-beacons", "textures/gui/container/beacon.png");


    @Inject(method = "init", at= @At("TAIL"))
    protected void init(CallbackInfo ci){
        BeaconScreen This = ((BeaconScreen)(Object)this);
        this.addBeaconButton(This.new BeaconPowerButton(This.leftPos, This.topPos, (BeaconBlockEntity.BEACON_EFFECTS.get(4)).getFirst(), false, 4));
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"))
    protected Identifier overrideBackground(Identifier originalImage){
        return NEW_BEACON_LOCATION;
    }

    @Definition(id = "beaconEffects", field = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;BEACON_EFFECTS:Ljava/util/List;")
    @Definition(id = "get", method = "Ljava/util/List;get(I)Ljava/lang/Object;")
    @Definition(id = "size", method = "Ljava/util/List;size()I")
    @Definition(id = "List", type = List.class)
    @Expression("((List)beaconEffects.get(3)).size() + 1")
    @Inject(method = "init", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.BY, by = -1), cancellable = true)
    private void initSecondaryPowers(CallbackInfo ci) {
        BeaconScreen This = ((BeaconScreen)(Object)this);
        int numSecondaryPowers = BeaconBlockEntity.BEACON_EFFECTS.subList(3, BeaconBlockEntity.BEACON_EFFECTS.size()).stream().mapToInt(Collection::size).sum();
        for (int l = 0; l < numSecondaryPowers; l++) {
            Holder<MobEffect> holder = BeaconBlockEntity.BEACON_EFFECTS.get(l + 3).getFirst();
            BeaconScreen.BeaconPowerButton beaconPowerButton = This.new BeaconPowerButton(
                    this.leftPos + 128 + l * 29, this.topPos + 47, holder, false, l + 3
            );
            beaconPowerButton.active = false;
            this.addBeaconButton(beaconPowerButton);
        }
        Holder<MobEffect> holder2 = (BeaconBlockEntity.BEACON_EFFECTS.getFirst()).getFirst();
        BeaconScreen.BeaconPowerButton beaconPowerButton2 = This.new BeaconUpgradePowerButton(
                this.leftPos + 157, this.topPos + 71, holder2
        );
        beaconPowerButton2.active = false;
        this.addBeaconButton(beaconPowerButton2);
        this.addBeaconButton(This.new BeaconConfirmButton(this.leftPos + 164, this.topPos + 107));
        this.addBeaconButton(This.new BeaconCancelButton(this.leftPos + 190, this.topPos + 107));
        ci.cancel();
    }
}
