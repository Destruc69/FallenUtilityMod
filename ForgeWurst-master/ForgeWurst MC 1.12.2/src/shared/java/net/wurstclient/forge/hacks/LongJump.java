/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.KeyBindingUtils;
import net.wurstclient.forge.utils.MathUtils;

public final class LongJump extends Hack {
	public static double startX;
	public static double startY;
	public static double startZ;

	private final SliderSetting motionY =
			new SliderSetting("MotionY", "How high we go", 0.405, 0.1, 1, 0.1, SliderSetting.ValueDisplay.INTEGER);

	private final SliderSetting dirSpeed =
			new SliderSetting("DirSpeed", "How fast we go", 1, 0.2, 4, 0.1, SliderSetting.ValueDisplay.INTEGER);

	public LongJump() {
		super("LongJump", "Jump far");
		setCategory(Category.MOVEMENT);
		addSetting(motionY);
		addSetting(dirSpeed);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
		try {
			startX = mc.player.posX;
			startY = mc.player.posY;
			startZ = mc.player.posZ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {
		if (mc.player.onGround) {
			mc.player.motionY = motionY.getValueF();
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindForward, true);
			double[] dir = MathUtils.directionSpeed(dirSpeed.getValueF());
			mc.player.motionX = dir[0];
			mc.player.motionZ = dir[1];
		}

		if (mc.player.onGround && mc.player.posX != startX && mc.player.posZ != startZ) {
			setEnabled(false);
			KeyBindingUtils.setPressed(mc.gameSettings.keyBindForward, false);
		}
	}

	@SubscribeEvent
	public void onPacket(WPacketInputEvent event) {
		if (event.getPacket() instanceof CPacketPlayer.Position) {
			event.setCanceled(true);
			mc.player.connection.sendPacket(new CPacketPlayer.Position(startX, startY, startZ, true));
		}
		if (event.getPacket() instanceof CPacketPlayer.PositionRotation) {
			event.setCanceled(true);
			mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(startX, startY, startZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
		}
		if (event.getPacket() instanceof CPacketPlayer.Rotation) {
			event.setCanceled(true);
			mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
		}
	}
}