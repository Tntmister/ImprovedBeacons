package tntmister.improvedbeacons;

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
}
