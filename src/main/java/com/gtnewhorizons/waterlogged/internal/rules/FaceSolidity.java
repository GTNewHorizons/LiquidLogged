package com.gtnewhorizons.waterlogged.internal.rules;

import net.minecraftforge.common.util.ForgeDirection;

/** Faces of vanilla holders that stop fluid, matching 26.2 shapes rather than Forge isSideSolid. */
public final class FaceSolidity {

    public enum Shape {
        STAIRS,
        SLAB,
        OTHER
    }

    private FaceSolidity() {}

    public static boolean isSolid(Shape shape, int meta, ForgeDirection side) {
        switch (shape) {
            case STAIRS: {
                boolean upsideDown = (meta & 4) != 0;
                if (side == ForgeDirection.UP) {
                    return upsideDown;
                }
                if (side == ForgeDirection.DOWN) {
                    return !upsideDown;
                }
                // Same back-face rule as Forge Block.isSideSolid for stairs: meta & 3 plus side ordinal is 5
                return (meta & 3) + side.ordinal() == 5;
            }
            case SLAB: {
                boolean top = (meta & 8) != 0;
                return top ? side == ForgeDirection.UP : side == ForgeDirection.DOWN;
            }
            default:
                return false;
        }
    }
}
