# LiquidLogged

LiquidLogged lets a block hold a fluid in the same cell, like waterlogging in modern Minecraft. Vanilla blocks follow Minecraft 26.2 and hold water only: stairs, single slabs, fences, glass panes, iron bars, walls, ladders, signs, chests, ender chests, trapdoors, rails and leaves. A block from another mod holds a fluid only if that mod opts in, and then it can hold any Forge fluid.

You fill a block by placing it into a source block or by pouring a bucket on it. An empty bucket takes the fluid back out. World generation and other mods do not fill blocks on their own. A filled block acts as a permanent source: water flows out through its open faces, you can swim in it, and it dims light and resists explosions like water. Breaking the block leaves a source behind, and a piston carries the fluid along with the block.

The mod stores fluids per chunk section through ChunkAPI, by fluid name, so EndlessIDs and block ID changes do not touch saved worlds. If you remove the mod, the blocks stay and lose their fluid; ChunkAPI warns about this when you open the world.

To let your block hold fluids, implement `IFluidHolder` on the Block, or register a predicate for a block you cannot change. Both run with the block and its tile entity already in the world, so one Block class can answer differently per tile entity:

```java
LiquidLoggedAPI.registerPredicate(myBlock, (world, x, y, z, block, meta, fluid) -> {
    TileEntity te = world.getTileEntity(x, y, z);
    return te instanceof MyFrame ? Boolean.TRUE : null;
});
LiquidLoggedAPI.setFluid(world, x, y, z, FluidRegistry.WATER);
```

Call `LiquidLoggedAPI.revalidate` after you change a tile entity in a way that changes that answer. A renderer with its own `IBlockAccess`, such as Angelica's `WorldSlice`, can implement `LiquidLoggedBlockAccess` and copy section data from `LiquidLoggedAPI.getSection`.

Operators can inspect and change stored fluids with `/liquidlogged get|set|clear <x> <y> <z> [fluid]`.
