package com.gtnewhorizons.waterlogged.mixins;

import javax.annotation.Nonnull;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import com.gtnewhorizon.gtnhmixins.builders.MixinBuilder;

public enum Mixins implements IMixins {

    // spotless:off
    SECTION_STORAGE(new MixinBuilder("Fluid storage in chunk sections")
            .addCommonMixins("minecraft.MixinExtendedBlockStorage")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    BLOCK_PACKET(new MixinBuilder("Fluid id in single block change packets")
            .addCommonMixins("minecraft.MixinS23PacketBlockChange")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    WORLD_ACCESS(new MixinBuilder("Read stored fluids from World and ChunkCache, block change rules")
            .addCommonMixins("minecraft.MixinWorld", "minecraft.MixinChunkCache")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    BUCKETS(new MixinBuilder("Buckets fill and empty blocks that hold a fluid")
            .addCommonMixins("minecraft.MixinItemBucket", "minecraft.MixinWorld_RayTrace")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    LIQUID_NEIGHBOURS(new MixinBuilder("Liquids see blocks holding a fluid as that fluid's source")
            .addCommonMixins("minecraft.MixinBlockLiquid", "minecraft.MixinBlockFluidClassic")
            .addClientMixins("minecraft.client.MixinBlockLiquid_Client", "minecraft.client.MixinBlockFluidBase")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    FLOW(new MixinBuilder("Stored fluid ticks and flow out of blocks")
            .addCommonMixins(
                "minecraft.MixinWorldServer",
                "minecraft.MixinBlockDynamicLiquid",
                "minecraft.AccessorBlockDynamicLiquid")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    ENTITIES(new MixinBuilder("Entities swim, drift and drown in stored fluid; explosions respect it")
            .addCommonMixins("minecraft.MixinWorld_Materials", "minecraft.MixinEntity", "minecraft.MixinExplosion")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    LIGHT(new MixinBuilder("Stored fluid dims and emits light like its source block")
            .addCommonMixins("minecraft.MixinChunk_Light", "minecraft.MixinWorld_Light", "minecraft.AccessorChunk")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    PISTONS(new MixinBuilder("Pistons move a block's stored fluid with the block")
            .addCommonMixins("minecraft.MixinBlockPistonBase", "minecraft.MixinTileEntityPiston")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY)),
    RENDER(new MixinBuilder("Draw the fluid stored in blocks")
            .addClientMixins(
                "minecraft.client.MixinWorldRenderer",
                "minecraft.client.MixinRenderBlocks",
                "minecraft.client.MixinRenderBlockFluid")
            .setApplyIf(() -> true)
            .setPhase(Phase.EARLY));
    // spotless:on

    private final MixinBuilder builder;

    Mixins(MixinBuilder builder) {
        this.builder = builder;
    }

    @Nonnull
    @Override
    public MixinBuilder getBuilder() {
        return builder;
    }
}
