/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ChunkCoordComparator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;
import net.wurstclient.forge.utils.TimerUtils;
import org.lwjgl.opengl.GL11;
import sun.net.www.http.ChunkedOutputStream;

import java.util.ArrayList;

public final class ChunkViewer extends Hack {

	ArrayList<AxisAlignedBB> loaded = new ArrayList<>();
	ArrayList<AxisAlignedBB> empty = new ArrayList<>();

	public ChunkViewer() {
		super("ChunkViewer", "shows if the chunk has been loaded.");
		setCategory(Category.RENDER);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
		TimerUtils.reset();
	}

	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		int radiusx = (int) mc.player.posX;
		int radiusz = (int) mc.player.posZ;
		for (int x = (int) -radiusx; x <= radiusx; x++) {
			for (int z = (int) -radiusz; z <= radiusz; z++) {
				Chunk chunk = new Chunk(mc.world, x, z);
				if (chunk.isEmpty()) {
					empty.add(BlockUtils.getBoundingBox(new BlockPos(x, mc.player.posY, z)));
				}
				if (chunk.isLoaded()) {
					loaded.add(BlockUtils.getBoundingBox(new BlockPos(x, mc.player.posY, z)));
				}
				if (TimerUtils.hasReached(1)) {
					TimerUtils.reset();
					empty.clear();
					loaded.clear();
				}
			}
		}
	}
	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(1);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
				-TileEntityRendererDispatcher.staticPlayerY,
				-TileEntityRendererDispatcher.staticPlayerZ);

		Vec3d start = RotationUtils.getClientLookVec()
				.addVector(0, 0, 0)
				.addVector(TileEntityRendererDispatcher.staticPlayerX,
						TileEntityRendererDispatcher.staticPlayerY,
						TileEntityRendererDispatcher.staticPlayerZ);

		for (AxisAlignedBB alignedBB : loaded) {
			GL11.glColor4f(0, 1, 0, 0.2F);
			GL11.glBegin(GL11.GL_LINES);
			RenderUtils.drawOutlinedBox(alignedBB);
			GL11.glEnd();
		}

		for (AxisAlignedBB alignedBB : empty) {
			GL11.glColor4f(1, 0, 0, 0.2F);
			GL11.glBegin(GL11.GL_LINES);
			RenderUtils.drawOutlinedBox(alignedBB);
			GL11.glEnd();
		}

		GL11.glPopMatrix();

		// GL resets
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
}