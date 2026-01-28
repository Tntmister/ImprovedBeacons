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
    default double improvedbeacons$getPower(){
        throw new UnsupportedOperationException();
    }
    default void improvedbeacons$setPower(double power){
        throw new UnsupportedOperationException();
    }
}
