package tntmister.morebeaconlevels;

import net.minecraft.world.level.block.Block;

import java.util.Optional;

public interface BeaconMajorityBlockController {
    default Optional<Block> morebeaconlevels$getMajorityBlock(){
         throw new UnsupportedOperationException();
    }
    default void morebeaconlevels$setMajorityBlock(Block block){
        throw new UnsupportedOperationException();
    }
    default double morebeaconlevels$getPower(){
        throw new UnsupportedOperationException();
    }
    default void morebeaconlevels$setPower(double power){
        throw new UnsupportedOperationException();
    }
}
