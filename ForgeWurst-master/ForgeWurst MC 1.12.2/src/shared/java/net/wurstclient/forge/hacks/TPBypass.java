/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WPacketOutputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.SliderSetting;

public final class TPBypass extends Hack {

	public static double x;
	public static double z;
	public static double y;

	private final SliderSetting xx =
			new SliderSetting("X Coord", "X coordinate", 0, 0, 99999999, 1, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting yy =
			new SliderSetting("Y Coord", "Y coordinate", 0, 0, 99999999, 1, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting zz =
			new SliderSetting("Z Coord", "Z coordinate", 0, 0, 99999999, 1, SliderSetting.ValueDisplay.DECIMAL);


	public TPBypass() {
		super("TPBypass", "A tp that can bypass.");
		setCategory(Category.MOVEMENT);
		addSetting(xx);
		addSetting(yy);
		addSetting(zz);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event) {

	}

	@SubscribeEvent
	public void onPacket(WPacketInputEvent event) {
		try {
			if (event.getPacket() instanceof CPacketPlayer) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer(true));
			}
			if (event.getPacket() instanceof CPacketPlayer.Rotation) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
			}
			if (event.getPacket() instanceof CPacketPlayer.Position) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.Position(xx.getValue(), yy.getValue(), zz.getValue(), true));
			}
			if (event.getPacket() instanceof CPacketPlayer.PositionRotation) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(xx.getValue(), yy.getValue(), zz.getValue(), mc.player.rotationYaw, mc.player.rotationPitch, true));
			}
			mc.player.setPosition(x, y, z);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onPackett(WPacketOutputEvent event) {
		try {
			if (event.getPacket() instanceof CPacketPlayer) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer(true));
			}
			if (event.getPacket() instanceof CPacketPlayer.Rotation) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
			}
			if (event.getPacket() instanceof CPacketPlayer.Position) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.Position(xx.getValue(), yy.getValue(), zz.getValue(), true));
			}
			if (event.getPacket() instanceof CPacketPlayer.PositionRotation) {
				event.setCanceled(true);
				mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(xx.getValue(), yy.getValue(), zz.getValue(), mc.player.rotationYaw, mc.player.rotationPitch, true));
			}
			mc.player.setPosition(x, y, z);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}