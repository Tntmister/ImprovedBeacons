package tntmister.improvedbeacons.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.inventory.BeaconMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tntmister.improvedbeacons.BeaconBlockEntityController;
import tntmister.improvedbeacons.ImprovedBeacons;
import tntmister.improvedbeacons.advancements.AdvancementCriteria;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityEntityMixin extends BlockEntity implements BeaconBlockEntityController {

    @Shadow
    public static final List<List<Holder<MobEffect>>> BEACON_EFFECTS = List.of(
            List.of(MobEffects.SPEED, MobEffects.HASTE),
            List.of(MobEffects.RESISTANCE, MobEffects.JUMP_BOOST),
            List.of(MobEffects.STRENGTH),
            List.of(MobEffects.REGENERATION),
            List.of(MobEffects.LUCK),
            List.of(MobEffects.HEALTH_BOOST)
    );

    @Shadow
    private static final Set<Holder<MobEffect>> VALID_EFFECTS = BEACON_EFFECTS.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

    @Unique
    Block majorityBlock;

    public BeaconBlockEntityEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public Optional<Block> improvedbeacons$getMajorityBlock() {
        return Optional.ofNullable(this.majorityBlock);
    }
    @Override
    public void improvedbeacons$setMajorityBlock(Block block) {
        this.majorityBlock = block;
    }

    // affects radius, 0 = full iron, 37 = full gold, 50 = full diamond/emerald, 100 = full netherite
    @Unique
    int power = 0;
    public int improvedbeacons$getPower() {
        return this.power;
    }
    public void improvedbeacons$setPower(int power) {
        this.power = power;
    }

    @Unique
    @Nullable
    Holder<MobEffect> tertiaryPower;
    public Holder<MobEffect> improvedbeacons$getTertiaryPower(){
        return this.tertiaryPower;
    }
    public void improvedbeacons$setTertiaryPower(Holder<MobEffect> tertiaryPower){
        this.tertiaryPower = tertiaryPower;
    }

    @Shadow
    private List<BeaconBeamOwner.Section> checkingBeamSections;
    @Shadow
    int levels;
    @Shadow
    Holder<MobEffect> primaryPower;
    @Shadow
    Holder<MobEffect> secondaryPower;

    @Shadow
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> BeaconBlockEntityEntityMixin.this.levels;
                case 1 -> BeaconMenu.encodeEffect(BeaconBlockEntityEntityMixin.this.primaryPower);
                case 2 -> BeaconMenu.encodeEffect(BeaconBlockEntityEntityMixin.this.secondaryPower);
                case 3 -> BeaconMenu.encodeEffect(BeaconBlockEntityEntityMixin.this.tertiaryPower);
                case 4 -> BeaconBlockEntityEntityMixin.this.power;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0:
                    BeaconBlockEntityEntityMixin.this.levels = value;
                    break;
                case 1:
                    assert BeaconBlockEntityEntityMixin.this.level != null;
                    if (!BeaconBlockEntityEntityMixin.this.level.isClientSide() && !BeaconBlockEntityEntityMixin.this.checkingBeamSections.isEmpty()) {
                        BeaconBlockEntity.playSound(BeaconBlockEntityEntityMixin.this.level, BeaconBlockEntityEntityMixin.this.worldPosition, SoundEvents.BEACON_POWER_SELECT);
                    }

                    BeaconBlockEntityEntityMixin.this.primaryPower = BeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(value));
                    break;
                case 2:
                    BeaconBlockEntityEntityMixin.this.secondaryPower = BeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(value));
                    break;
                case 3:
                    BeaconBlockEntityEntityMixin.this.tertiaryPower = BeaconBlockEntity.filterEffect(BeaconMenu.decodeEffect(value));
                    break;
                case 4:
                    BeaconBlockEntityEntityMixin.this.power = value;
            }
        }

        @Override
        public int getCount() {
            return BeaconBlockEntity.NUM_DATA_VALUES + ImprovedBeacons.DATA_NUM_VALUES;
        }
    };

    //ideas
    //reword gui and functionality
    //only allow supplied item to be the material of the majority of the composition (or higher rarity)
    //layer 5 power: TODO
    //layer 6 power: TODO

    @Unique
    private static final int NEW_MAX_LEVELS = 6;
    @Unique
    private static final Map<Block, Integer> BLOCK_POWER = new HashMap<>(){{
        put(Blocks.IRON_BLOCK, 0);
        put(Blocks.GOLD_BLOCK, 37);
        put(Blocks.DIAMOND_BLOCK, 50);
        put(Blocks.EMERALD_BLOCK, 50);
        put(Blocks.NETHERITE_BLOCK, 100);
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

    @Inject(method = "updateBase", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getMinY()I"))
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
                blockIntegerEntry -> {
                    if (blockIntegerEntry.getValue() > 0) beaconBlockController.improvedbeacons$setMajorityBlock(blockIntegerEntry.getKey());
                },
                () -> beaconBlockController.improvedbeacons$setMajorityBlock(null)
        );

        // calculates the beacon's power based on the average sum of the power of the blocks that make up the pyramid
        // example: a beacon that's 50% iron and 50% netherite has power 0.5 * 0 + 0.5 * 100 = 50
        int totalBlocks = blockMap.values().stream().reduce(0, Integer::sum);
        AtomicReference<Integer> power = new AtomicReference<>(0);
        if (beaconBlockController.improvedbeacons$getMajorityBlock().isPresent())
            blockMap.forEach((block, count) -> power.updateAndGet(v -> (v + count * BLOCK_POWER.get(block) / totalBlocks)));
        beaconBlockController.improvedbeacons$setPower(power.get());
    }

    // new beacon radius formula
    // base radius of 16, with additional 10 radius per pyramid layer, increased
    @ModifyVariable(method = "applyEffects", at = @At("STORE"), name = "d")
    private static double applyEffects(double radius, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos blockPos, @Local(argsOnly = true) int beaconLevel){
        return level.getBlockEntity(blockPos, BlockEntityType.BEACON).map(blockEntity ->
                (1 +  3 * (double)((BeaconBlockEntityController) blockEntity).improvedbeacons$getPower() / 100) * (beaconLevel * 8 + 16)
        ).orElse(radius);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/ConstructBeaconTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;I)V"))
    private static void triggerAdvancement(Level level, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity, CallbackInfo ci, @Local(name = "serverPlayer") ServerPlayer serverPlayer){
        AdvancementCriteria.MAXED_BEACON.trigger(serverPlayer, ((BeaconBlockEntityController)blockEntity).improvedbeacons$getPower());
    }

    // fixes Beaconator advancement for levels > 4
    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/ConstructBeaconTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;I)V"))
    private static int beaconatorLevelsFix(int levels){
        return Math.min(levels, 4);
    }
}