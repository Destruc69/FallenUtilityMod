/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.MathUtils;
import net.wurstclient.forge.utils.PlayerUtils;
import net.wurstclient.forge.utils.TimerUtils;

public final class NoSlowDown extends Hack {
	private final CheckboxSetting ncp =
			new CheckboxSetting("OldNCP", "Tells the server you stopped using the item \n" +
					"when in reality you are still using it, Bypasses old versions of NCP ",
					false);

	private final CheckboxSetting bt =
			new CheckboxSetting("SneakingPacket", "Sends start sneaking packet \n" +
					"This might bypass some NCP configs because i tested on 2b2t \n" +
					"and when you are sneaking it goes slightly slower but bypasses",
					false);

	public NoSlowDown() {
		super("NoSlowDown", "No time to slow down when eating");
		setCategory(Category.MOVEMENT);
		addSetting(ncp);
		addSetting(bt);
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
		try {
			if (mc.player.isHandActive() && mc.player.getHeldItemMainhand().getItem() instanceof Item) {
				if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
					double[] dir = MathUtils.directionSpeed(0.2);
					mc.player.motionX = dir[0];
					mc.player.motionZ = dir[1];
				}

				if (bt.isChecked()) {
					if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
						mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
					}
				}

				if (ncp.isChecked()) {
					mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, PlayerUtils.GetLocalPlayerPosFloored(), EnumFacing.DOWN));
				}
			} else {
				if (bt.isChecked()) {
					mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}