package com.gtnewhorizons.liquidlogged.internal.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraftforge.common.util.ForgeDirection;

import org.junit.jupiter.api.Test;

import com.gtnewhorizons.liquidlogged.internal.rules.FaceSolidity.Shape;

class FaceSolidityTest {

    @Test
    void bottomStairsBlocksDown() {
        assertTrue(FaceSolidity.isSolid(Shape.STAIRS, 0, ForgeDirection.DOWN));
    }

    @Test
    void bottomStairsOpenUp() {
        assertFalse(FaceSolidity.isSolid(Shape.STAIRS, 0, ForgeDirection.UP));
    }

    @Test
    void topStairsBlocksUp() {
        assertTrue(FaceSolidity.isSolid(Shape.STAIRS, 4, ForgeDirection.UP));
        assertFalse(FaceSolidity.isSolid(Shape.STAIRS, 4, ForgeDirection.DOWN));
    }

    @Test
    void stairsBackFaceByMeta() {
        assertTrue(FaceSolidity.isSolid(Shape.STAIRS, 0, ForgeDirection.EAST));
        assertTrue(FaceSolidity.isSolid(Shape.STAIRS, 1, ForgeDirection.WEST));
        assertTrue(FaceSolidity.isSolid(Shape.STAIRS, 2, ForgeDirection.SOUTH));
        assertTrue(FaceSolidity.isSolid(Shape.STAIRS, 3, ForgeDirection.NORTH));
        assertFalse(FaceSolidity.isSolid(Shape.STAIRS, 0, ForgeDirection.WEST));
        assertFalse(FaceSolidity.isSolid(Shape.STAIRS, 2, ForgeDirection.NORTH));
    }

    @Test
    void bottomSlabBlocksDownOnly() {
        assertTrue(FaceSolidity.isSolid(Shape.SLAB, 0, ForgeDirection.DOWN));
        assertFalse(FaceSolidity.isSolid(Shape.SLAB, 0, ForgeDirection.UP));
        assertFalse(FaceSolidity.isSolid(Shape.SLAB, 0, ForgeDirection.NORTH));
    }

    @Test
    void topSlabBlocksUpOnly() {
        assertTrue(FaceSolidity.isSolid(Shape.SLAB, 8, ForgeDirection.UP));
        assertFalse(FaceSolidity.isSolid(Shape.SLAB, 8, ForgeDirection.DOWN));
    }

    @Test
    void leavesBlockAllSides() {
        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            assertTrue(FaceSolidity.isSolid(Shape.LEAVES, 0, d));
        }
    }

    @Test
    void closedBottomTrapdoorBlocksDownOnly() {
        assertTrue(FaceSolidity.isSolid(Shape.TRAPDOOR, 0, ForgeDirection.DOWN));
        assertFalse(FaceSolidity.isSolid(Shape.TRAPDOOR, 0, ForgeDirection.UP));
        assertFalse(FaceSolidity.isSolid(Shape.TRAPDOOR, 0, ForgeDirection.SOUTH));
    }

    @Test
    void closedTopTrapdoorBlocksUpOnly() {
        assertTrue(FaceSolidity.isSolid(Shape.TRAPDOOR, 8, ForgeDirection.UP));
        assertFalse(FaceSolidity.isSolid(Shape.TRAPDOOR, 8, ForgeDirection.DOWN));
    }

    @Test
    void openTrapdoorBlocksHingeSide() {
        assertTrue(FaceSolidity.isSolid(Shape.TRAPDOOR, 4, ForgeDirection.SOUTH));
        assertTrue(FaceSolidity.isSolid(Shape.TRAPDOOR, 4 | 1, ForgeDirection.NORTH));
        assertTrue(FaceSolidity.isSolid(Shape.TRAPDOOR, 4 | 2, ForgeDirection.EAST));
        assertTrue(FaceSolidity.isSolid(Shape.TRAPDOOR, 4 | 3, ForgeDirection.WEST));
        assertFalse(FaceSolidity.isSolid(Shape.TRAPDOOR, 4, ForgeDirection.DOWN));
        assertFalse(FaceSolidity.isSolid(Shape.TRAPDOOR, 4, ForgeDirection.NORTH));
    }

    @Test
    void otherNeverSolid() {
        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            assertFalse(FaceSolidity.isSolid(Shape.OTHER, 0, d));
        }
    }
}
