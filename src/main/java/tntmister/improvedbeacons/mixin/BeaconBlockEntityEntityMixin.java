package tntmister.improvedbeacons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tntmister.improvedbeacons.BeaconBlockEntityController;
import tntmister.improvedbeacons.advancements.AdvancementCriteria;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityEntityMixin implements BeaconBlockEntityController {

    @Unique
    Block majorityBlock;

    @Override
    public Optional<Block> improvedbeacons$getMajorityBlock() {
        return Optional.ofNullable(this.majorityBlock);
    }

    @Override
    public void improvedbeacons$setMajorityBlock(Block block) {
        this.majorityBlock = block;
    }

    // affects radius, 1 = full iron, 1.5 = full gold, 2 = full diamond/emerald, 4 = full netherite
    @Unique
    double power = 1;

    public double improvedbeacons$getPower() {
        return this.power;
    }

    public void improvedbeacons$setPower(double power) {
        this.power = power;
    }

    //ideas
    //reword gui and functionality
    //only allow supplied item to be the material of the majority of the composition (or higher rarity)
    //layer 5 power: TODO
    //layer 6 power: TODO

    @Unique
    private static final int NEW_MAX_LEVELS = 6;
    @Unique
    private static final Map<Block, Double> BLOCK_POWER = new HashMap<>(){{
        put(Blocks.IRON_BLOCK, 1.0);
        put(Blocks.GOLD_BLOCK, 1.5);
        put(Blocks.DIAMOND_BLOCK, 2.0);
        put(Blocks.EMERALD_BLOCK, 2.0);
        put(Blocks.NETHERITE_BLOCK, 4.0);
    }};

    @Inject(method = "updateBase", at = @At("HEAD"))
    private static void updateBaseHEAD(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir, @Share("blockEntity") LocalRef<BeaconBlockEntity> beaconBlockEntityRef, @Share("blockMap") LocalRef<Map<Block, Integer>> blockMapRef){
        beaconBlockEntityRef.set(level.getBlockEntity(new BlockPos(x, y ,z), BlockEntityType.BEACON).orElseThrow());
        blockMapRef.set(new HashMap<>());
    }

    @ModifyConstant(method = "updateBase", constant = @Constant(intValue = 4))
    private static int updateBaseMaxLevels(int value){
        return NEW_MAX_LEVELS;
    }

    @Inject(method = "updateBase", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getMinBuildHeight()I"))
    private static void updateBaseLayerLoopHEAD(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir, @Share("blockLayerMap") LocalRef<Map<Block, Integer>> blockLayerMapRef){
        blockLayerMapRef.set(new HashMap<>());
    }

    @ModifyExpressionValue(method = "updateBase", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private static BlockState updateBaseInnerLoop(BlockState blockState, @Share("blockMap") LocalRef<Map<Block, Integer>> blockMapRef, @Share("blockLayerMap") LocalRef<Map<Block, Integer>> blockLayerMapRef){
        if (blockState.is(BlockTags.BEACON_BASE_BLOCKS)) {
            blockMapRef.get().merge(blockState.getBlock(), 1, Integer::sum);
            blockLayerMapRef.get().merge(blockState.getBlock(), 1, Integer::sum);
        } else {
            blockLayerMapRef.get().forEach(((block, count) -> blockMapRef.get().merge(block, -count, Integer::sum)));
        }
        return blockState;
    }

    @Inject(method = "updateBase", at = @At("TAIL"))
    private static void updateBaseTAIL(Level level, int x, int y, int z, CallbackInfoReturnable<Integer> cir, @Share("blockEntity") LocalRef<BeaconBlockEntity> beaconBlockEntityRef, @Share("blockMap") LocalRef<Map<Block, Integer>> blockMapRef){
        BeaconBlockEntityController beaconBlockController = ((BeaconBlockEntityController) beaconBlockEntityRef.get());
        Map<Block, Integer> blockMap = blockMapRef.get();
        // find the most common block in the pyramid
        blockMap.entrySet().stream().max(Map.Entry.comparingByValue()).ifPresentOrElse(
                blockIntegerEntry -> beaconBlockController.improvedbeacons$setMajorityBlock(blockIntegerEntry.getKey()),
                () -> beaconBlockController.improvedbeacons$setMajorityBlock(null)
        );

        // calculates the beacon's power based on the average sum of the power of the blocks that make up the pyramid
        // example: a beacon that's 50% iron and 50% netherite has power 0.5 * 1 + 0.5 * 4 = 2.5
        int totalBlocks = blockMap.values().stream().reduce(0, Integer::sum);
        AtomicReference<Double> power = new AtomicReference<>(0.0);
        if (beaconBlockController.improvedbeacons$getMajorityBlock().isPresent())
            blockMap.forEach((block, count) -> power.updateAndGet(v -> (v + count * BLOCK_POWER.get(block) / totalBlocks)));
        else power.set(1.0);
        beaconBlockController.improvedbeacons$setPower(power.get());
    }

    // new beacon radius formula
    // base radius of 16, with additional 10 radius per pyramid layer, increased
    @ModifyVariable(method = "applyEffects", at = @At("STORE"), name = "d")
    private static double applyEffects(double radius, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos blockPos, @Local(argsOnly = true) int beaconLevel){
        return level.getBlockEntity(blockPos, BlockEntityType.BEACON).map(blockEntity ->
                (1 +  3 * ((BeaconBlockEntityController) blockEntity).improvedbeacons$getPower() / 100) * (beaconLevel * 8 + 16)
        ).orElse(radius);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ConstructBeaconTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;I)V"))
    private static void triggerAdvancement(Level level, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci, @Local(name = "serverPlayer") ServerPlayer serverPlayer){
        AdvancementCriteria.MAXED_BEACON.trigger(serverPlayer, ((BeaconBlockEntityController)blockEntity).improvedbeacons$getPower());
    }

    // fixes Beaconator advancement for levels > 4
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/ConstructBeaconTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;I)V"))
    private static int injected(int levels){
        return Math.min(levels, 4);
    }
}