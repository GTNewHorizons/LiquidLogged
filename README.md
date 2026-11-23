# LiquidLogged

LiquidLogged lets a block hold a fluid in the same cell, like waterlogging in modern Minecraft. Vanilla blocks follow Minecraft 26.2 and hold water only: stairs, single slabs, fences, glass panes, iron bars, walls, ladders, signs, chests, ender chests, trapdoors, rails and leaves. A block from another mod holds a fluid only if that mod opts in, and then it can hold any Forge fluid.

## Required Dependencies

* [ChunkAPI 0.8.4](https://github.com/LegacyModdingMC/ChunkAPI)
* [UniMixins 0.3.1](https://github.com/LegacyModdingMC/UniMixins)

## Usage

Place a supported block into a fluid source or pour a bucket into it to store a source. You can pick that source back up with a bucket. Breaking the block leaves the source behind.

Flowing water can pass through supported blocks when both faces at each boundary are open. The water drains when its supply stops. You can pour a bucket into a block that holds flowing water to make it a source.

The mod stores fluids per chunk section through ChunkAPI, by fluid name, so EndlessIDs and block ID changes do not touch saved worlds. If you remove the mod, the blocks stay and lose their fluid; ChunkAPI warns about this when you open the world.

To let your block hold fluids, implement `IFluidHolder` on the Block, or register a predicate for a block you cannot change. Both run with the block and its tile entity already in the world, so one Block class can answer differently per tile entity:

```java
LiquidLoggedAPI.registerPredicate(myBlock, (world, x, y, z, block, meta, fluid) -> {
    TileEntity te = world.getTileEntity(x, y, z);
    return te instanceof MyFrame ? Boolean.TRUE : null;
});
LiquidLoggedAPI.setFluid(world, x, y, z, FluidRegistry.WATER);
```

Call `LiquidLoggedAPI.revalidate` after you change a tile entity in a way that changes that answer. A renderer with its own `IBlockAccess`, such as Angelica's `WorldSlice`, can implement `LiquidLoggedBlockAccess` and copy section data from `LiquidLoggedAPI.getSection`. A mesher that draws stored fluids itself calls `LiquidLoggedRenderAPI.beginStoredFluid` and `endStoredFluid` around the draw, and `isFaceCovered` to skip faces a block hides.

Operators can inspect and change stored fluids with `/liquidlogged get|set|clear <x> <y> <z> [fluid]`.
