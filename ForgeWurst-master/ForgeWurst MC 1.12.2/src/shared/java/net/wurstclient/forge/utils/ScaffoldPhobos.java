/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.forge.Hack;

public final class ScaffoldPhobos extends Hack {

    public ScaffoldPhobos(String name, String description) {
        super(name, description);
    }

    public static void scaffoldCore() {
        BlockPos playerBlock;

        if (BlockUtils.isScaffoldPos((playerBlock = BlockUtils.getPlayerPosWithEntity()).add(0, -1, 0))) {
            if (BlockUtils.isValidBlock(playerBlock.add(0, -2, 0))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.UP);
            } else if (BlockUtils.isValidBlock(playerBlock.add(-1, -1, 0))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.EAST);
            } else if (BlockUtils.isValidBlock(playerBlock.add(1, -1, 0))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.WEST);
            } else if (BlockUtils.isValidBlock(playerBlock.add(0, -1, -1))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.SOUTH);
            } else if (BlockUtils.isValidBlock(playerBlock.add(0, -1, 1))) {
                place(playerBlock.add(0, -1, 0), EnumFacing.NORTH);
            } else if (BlockUtils.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtils.isValidBlock(playerBlock.add(0, -1, 1))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.NORTH);
                }
                place(playerBlock.add(1, -1, 1), EnumFacing.EAST);
            } else if (BlockUtils.isValidBlock(playerBlock.add(-1, -1, 1))) {
                if (BlockUtils.isValidBlock(playerBlock.add(-1, -1, 0))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.WEST);
                }
                place(playerBlock.add(-1, -1, 1), EnumFacing.SOUTH);
            } else if (BlockUtils.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtils.isValidBlock(playerBlock.add(0, -1, 1))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.SOUTH);
                }
                place(playerBlock.add(1, -1, 1), EnumFacing.WEST);
            } else if (BlockUtils.isValidBlock(playerBlock.add(1, -1, 1))) {
                if (BlockUtils.isValidBlock(playerBlock.add(0, -1, 1))) {
                    place(playerBlock.add(0, -1, 1), EnumFacing.EAST);
                }
                place(playerBlock.add(1, -1, 1), EnumFacing.NORTH);
            }
        }
    }

    public static void place(BlockPos posI, EnumFacing face) {
        Block block;
        BlockPos pos = posI;
        if (face == EnumFacing.UP) {
            pos = pos.add(0, -1, 0);
        } else if (face == EnumFacing.NORTH) {
            pos = pos.add(0, 0, 1);
        } else if (face == EnumFacing.SOUTH) {
            pos = pos.add(0, 0, -1);
        } else if (face == EnumFacing.EAST) {
            pos = pos.add(-1, 0, 0);
        } else if (face == EnumFacing.WEST) {
            pos = pos.add(1, 0, 0);
        }

        if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock))
            return;

        mc.playerController.processRightClickBlock(mc.player, mc.world, pos, face, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }
}