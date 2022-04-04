/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WPlayerMoveEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.SliderSetting;

public final class FreeCam extends Hack
{
	public static EntityPlayer camera;

	private final SliderSetting speed =
			new SliderSetting("Speed", 1, 0.05, 10, 0.05, SliderSetting.ValueDisplay.DECIMAL);

	public FreeCam()
	{
		super("Fullbright", "Allows you to see in the dark.");
		setCategory(Category.RENDER);
	}

	@Override
	public void onEnable() {
		if (mc.player == null || mc.world == null)
			return;

		MinecraftForge.EVENT_BUS.register(this);
		mc.renderChunksMany = false;

		camera = new EntityOtherPlayerMP(mc.world, mc.getSession().getProfile());
		camera.copyLocationAndAnglesFrom(mc.player);
		camera.prevRotationYaw = mc.player.rotationYaw;
		camera.rotationYawHead = mc.player.rotationYawHead;
		camera.inventory.copyInventory(mc.player.inventory);
		mc.world.addEntityToWorld(-100, camera);
		mc.setRenderViewEntity(camera);
	}

	@Override
	public void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		mc.renderChunksMany = true;

		if (mc.player != null && mc.world != null && mc.getRenderViewEntity() != null) {
			mc.player.moveStrafing = 0;
			mc.player.moveForward = 0;
			mc.world.removeEntity(camera);
			mc.setRenderViewEntity(mc.player);
		}
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent e) {
		if (!e.getEntity().equals(camera) || mc.currentScreen != null) {
			return;
		}

		if (camera == null)
			return;

		//Update motion
		if (mc.gameSettings.keyBindJump.isKeyDown()) {
			camera.motionY = speed.getValueF();
		} else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
			camera.motionY = -speed.getValueF();
		} else {
			camera.motionY = 0;
		}

		if (mc.gameSettings.keyBindForward.isKeyDown()) {
			camera.moveForward = 1;
		} else if (mc.gameSettings.keyBindBack.isKeyDown()) {
			camera.moveForward = -1;
		} else {
			camera.moveForward = 0;
		}

		if (mc.gameSettings.keyBindLeft.isKeyDown()) {
			camera.moveStrafing = -1;
		} else if (mc.gameSettings.keyBindRight.isKeyDown()) {
			camera.moveStrafing = 1;
		} else {
			camera.moveStrafing = 0;
		}

		if (camera.moveStrafing != 0 || camera.moveForward != 0) {
			double yawRad = Math.toRadians(camera.rotationYaw - getRotationFromVec(new Vec3d(camera.moveStrafing, 0.0, camera.moveForward))[0]);

			camera.motionX = -Math.sin(yawRad) * speed.getValueF();
			camera.motionZ = Math.cos(yawRad) * speed.getValueF();

			if (mc.gameSettings.keyBindSprint.isKeyDown()) {
				camera.setSprinting(true);
				camera.motionX *= 1.5;
				camera.motionZ *= 1.5;
			} else {
				camera.setSprinting(false);
			}
		} else {
			camera.motionX = 0;
			camera.motionZ = 0;
		}

		camera.inventory.copyInventory(mc.player.inventory);
		camera.noClip = true;
		camera.rotationYawHead = camera.rotationYaw;

		camera.move(MoverType.SELF, camera.motionX, camera.motionY, camera.motionZ);
	}

	@SubscribeEvent
	private void packet(WPacketInputEvent event) {
		try {
			if (event.getPacket() instanceof CPacketUseEntity) {
				CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();

				if (packet.getEntityFromWorld(mc.world).equals(mc.player)) {
					event.setCanceled(true);
				}
			}
		} catch (NullPointerException e) {
		}
	}

	@SubscribeEvent
	private void move(WPlayerMoveEvent event) {
		mc.player.movementInput.moveForward = 0;
		mc.player.movementInput.moveStrafe = 0;
		mc.player.movementInput.jump = false;
		mc.player.movementInput.sneak = false;
		mc.player.setSprinting(false);

		event.setCanceled(true);
	}

	public static double[] getRotationFromVec(Vec3d vec) {
		double xz = Math.sqrt(vec.x * vec.x + vec.z * vec.z);
		double yaw = normalizeAngle(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0);
		double pitch = normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
		return new double[]{yaw, pitch};
	}

	public static double normalizeAngle(double angle) {
		angle %= 360.0;

		if (angle >= 180.0) {
			angle -= 360.0;
		}

		if (angle < -180.0) {
			angle += 360.0;
		}

		return angle;
	}
}


