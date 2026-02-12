package tntmister.improvedbeacons;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public interface BeaconBlockEntityController {
    default Optional<Block> improvedbeacons$getMajorityBlock(){
         throw new UnsupportedOperationException();
    }
    default void improvedbeacons$setMajorityBlock(Block block){
        throw new UnsupportedOperationException();
    }
    default int improvedbeacons$getPower(){
        throw new UnsupportedOperationException();
    }
    default void improvedbeacons$setPower(int power){
        throw new UnsupportedOperationException();
    }
    default Holder<MobEffect> improvedbeacons$getTertiaryPower() {
        throw new UnsupportedOperationException();
    }
    default void improvedbeacons$setTertiaryPower(Holder<MobEffect> tertiaryPower) {
        throw new UnsupportedOperationException();
    }
}
