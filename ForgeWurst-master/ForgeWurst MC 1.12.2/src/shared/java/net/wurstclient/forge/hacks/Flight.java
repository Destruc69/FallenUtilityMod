/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WPacketOutputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.MathUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.TimerUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public final class Flight extends Hack {

	private final EnumSetting<Mode> mode =
			new EnumSetting<>("Mode", Mode.values(), Mode.NCP);

	private enum Mode {
		NORMAL("Normal", true, false, false, false, false, false),
		NCP("NCP", false, true, false, false, false, false),
		JUMP("Jump", false, false, true, false, false, false),
		STATIC("Static", false, false, false, true, false, false),
		D3("3D", false, false, false, false, true, false),
		SPOOF("Spoof", false, false, false, false, false, true);

		private final String name;
		private final boolean normal;
		private final boolean ncp;
		private final boolean jump;
		private final boolean staticc;
		private final boolean d3;
		private final boolean spoof;

		private Mode(String name, boolean normal, boolean ncp, boolean jump, boolean staticc, boolean d3, boolean spoof) {
			this.name = name;
			this.normal = normal;
			this.ncp = ncp;
			this.jump = jump;
			this.staticc = staticc;
			this.d3 = d3;
			this.spoof = spoof;
		}

		public String toString() {
			return name;
		}
	}

	ArrayList<AxisAlignedBB> bbs = new ArrayList<>();

	public static int aimY;

	public Flight() {
		super("Flight", "I believe i can fly.");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
		aimY = (int) mc.player.posY;
	}

	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		if (mode.getSelected().d3) {
			BlockPos pos = new BlockPos(mc.objectMouseOver.getBlockPos());
			bbs.add(BlockUtils.getBoundingBox(pos));
			if (TimerUtils.hasReached(10)) {
				mc.player.setPosition(pos.getX(), pos.getY(), pos.getZ());
			} else {
				TimerUtils.reset();
				mc.player.motionX = 0;
				mc.player.motionY = 0;
				mc.player.motionZ = 0;
				mc.player.setVelocity(0, 0, 0);
			}
		}
		if (mode.getSelected().normal) {
			EntityPlayerSP player = event.getPlayer();

			player.capabilities.isFlying = false;
			player.motionX = 0;
			player.motionY = 0;
			player.motionZ = 0;
			player.jumpMovementFactor = 1;

			bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)));

			if (mc.gameSettings.keyBindJump.isKeyDown()) {
				player.motionY += 1;
			}

			if (mc.gameSettings.keyBindSneak.isKeyDown()) {
				player.motionY -= 1;
			}
		}

		if (mode.getSelected().staticc) {
			if (TimerUtils.hasReached(50)) {
				EntityPlayerSP player = event.getPlayer();

				player.capabilities.isFlying = false;
				player.motionX = 0;
				player.motionY = 0;
				player.motionZ = 0;
				player.jumpMovementFactor = 10;
				bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX, mc.player.posY + 2, mc.player.posZ)));

				bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)));

				if (mc.gameSettings.keyBindJump.isKeyDown()) {
					player.motionY += 10;
				}

				if (mc.gameSettings.keyBindSneak.isKeyDown()) {
					player.motionY -= 10;
				}
			} else {
				bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)));
				TimerUtils.reset();
				mc.player.motionX = 0;
				mc.player.motionY = 0;
				mc.player.motionZ = 0;
				mc.player.setVelocity(0, 0, 0);
			}
		}

		if (TimerUtils.hasReached(1)) {
			bbs.clear();
			TimerUtils.reset();
		}

		if (mode.getSelected().ncp) {
			EntityPlayerSP player = event.getPlayer();

			player.capabilities.isFlying = false;
			player.motionX = 0;
			player.motionY = 0;
			player.motionZ = 0;
			player.jumpMovementFactor = (float) (Math.random() * 1);

			if (mc.gameSettings.keyBindJump.isKeyDown()) {
				player.motionY += Math.random() * 1;
			}

			if (mc.gameSettings.keyBindSneak.isKeyDown()) {
				player.motionY -= Math.random() * 1;
			}

			if (mc.player.posY + 2 > aimY + 2) {
				mc.player.setPosition(mc.player.posX, aimY, mc.player.posZ);
			}

			mc.player.connection.sendPacket(new CPacketInput(mc.player.moveStrafing, mc.player.moveForward, true, true));
			mc.player.connection.sendPacket(new CPacketInput(mc.player.moveStrafing, mc.player.moveForward, true, true));

			mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY - Math.random() * 0.5, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));

			int radius = 2;
			for (int x = (int) -radius; x <= radius; x++) {
				for (int z = (int) -radius; z <= radius; z++) {
					bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX + x, aimY + 2, mc.player.posZ + z).subtract(new Vec3i(0, 0.5, 1))));
				}
			}
		}
		if (mode.getSelected().jump) {
			if (mc.player.posY < aimY) {
				mc.player.jump();
			}

			bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX, aimY - 1, mc.player.posZ)));
		}
		if (mode.getSelected().spoof) {
			double x = mc.player.posX + mc.player.motionX;
			double z = mc.player.posZ + mc.player.motionZ;

			if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
				mc.player.setPosition(x, aimY, z);
			}

			mc.player.setPosition(mc.player.posX, aimY, mc.player.posZ);
			mc.player.motionY = 0;
			mc.player.setVelocity(mc.player.motionX, 0, mc.player.motionZ);

			bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX, aimY - 1, mc.player.posZ)));
			bbs.add(BlockUtils.getBoundingBox(new BlockPos(mc.player.posX, aimY + 2, mc.player.posZ)));
		}
	}

	@SubscribeEvent
	public void onPacket(WPacketInputEvent event) {
		double x = mc.player.posX + mc.player.motionX;
		double z = mc.player.posZ + mc.player.motionZ;
		if (mode.getSelected().spoof) {
			if (event.getPacket() instanceof CPacketPlayer.Position) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.Position(x, aimY, z, mc.player.onGround));
			}
			if (event.getPacket() instanceof CPacketPlayer.Rotation) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
			}
			if (event.getPacket() instanceof CPacketPlayer.PositionRotation) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(x, aimY, z, mc.player.rotationYaw, mc.player.rotationPitch, mc.player.onGround));
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

		for (AxisAlignedBB alignedBB : bbs) {
			GL11.glColor4f(0, 1, 0, 0.5F);
			GL11.glBegin(GL11.GL_QUADS);
			RenderUtils.drawSolidBox(alignedBB);
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