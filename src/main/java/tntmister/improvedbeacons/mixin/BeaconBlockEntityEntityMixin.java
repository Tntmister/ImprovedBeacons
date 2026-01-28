package tntmister.improvedbeacons.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import tntmister.improvedbeacons.BeaconBlockEntityController;

import java.util.*;
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

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;updateBase(Lnet/minecraft/world/level/Level;III)I"))
    private static int updateBase(Level level, int x, int y, int z, @Local(argsOnly = true) BeaconBlockEntity beaconBlockEntity) {
        int completeLayers = 0;

        Map<Block, Integer> blockMap = new HashMap<>();

        for (int layer = 1; layer <= NEW_MAX_LEVELS; completeLayers = layer++) {
            int layerY = y - layer;
            if (layerY < level.getMinBuildHeight()) {
                break;
            }

            boolean bl = true;

            Map<Block, Integer> blockLayerMap = new HashMap<>();

            for (int offsetX = x - layer; offsetX <= x + layer && bl; offsetX++) {
                for (int offsetZ = z - layer; offsetZ <= z + layer; offsetZ++) {
                    BlockState blockState = level.getBlockState(new BlockPos(offsetX, layerY, offsetZ));
                    if (!blockState.is(BlockTags.BEACON_BASE_BLOCKS)) {
                        bl = false;
                        break;
                    } else {
                        blockLayerMap.merge(blockState.getBlock(), 1, Integer::sum);
                    }
                }
            }

            if (!bl) {
                break;
            }
            blockMap.putAll(blockLayerMap);
        }
        BeaconBlockEntityController beaconBlockController = ((BeaconBlockEntityController) beaconBlockEntity);
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

        return completeLayers;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeaconBlockEntity;applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V"))
    private static void applyEffects(Level level, BlockPos blockPos, int i, @Nullable Holder<MobEffect> holder, @Nullable Holder<MobEffect> holder2, @Local(argsOnly = true) BeaconBlockEntity beaconBlockEntity) {
        if (!level.isClientSide && holder != null) {
            double d = ((BeaconBlockEntityController) beaconBlockEntity).improvedbeacons$getPower() * i * 10 + 16;
            int j = 0;
            if (i >= 4 && Objects.equals(holder, holder2)) {
                j = 1;
            }

            int k = (9 + i * 2) * 20;
            AABB aABB = new AABB(blockPos).inflate(d).expandTowards(0.0, level.getHeight(), 0.0);
            List<Player> list = level.getEntitiesOfClass(Player.class, aABB);

            for (Player player : list) {
                player.addEffect(new MobEffectInstance(holder, k, j, true, true));
            }

            if (i >= 4 && !Objects.equals(holder, holder2) && holder2 != null) {
                for (Player player : list) {
                    player.addEffect(new MobEffectInstance(holder2, k, 0, true, true));
                }
            }
        }
    }
}