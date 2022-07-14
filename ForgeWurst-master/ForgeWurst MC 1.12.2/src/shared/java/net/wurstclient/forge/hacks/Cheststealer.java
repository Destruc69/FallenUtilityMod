/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.InventoryUtil;
import net.wurstclient.forge.utils.TimerUtils;

public final class Cheststealer extends Hack {

	public static double slotToPick;

	private final CheckboxSetting bypass =
			new CheckboxSetting("Bypass", "A few sneaky ways to get around anti cheats",
					false);

	public Cheststealer() {
		super("ChestStealer", "Steals items from chest.");
		setCategory(Category.PLAYER);
		addSetting(bypass);
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
		if (mc.currentScreen instanceof GuiChest) {
			GuiChest guiChest = (GuiChest) mc.currentScreen;
			mc.playerController.updateController();
			if (TimerUtils.hasReached(100) && slotToPick < 27) {
				slotToPick = slotToPick + 1;
				TimerUtils.reset();
			}
			if (!guiChest.inventorySlots.getSlot((int) slotToPick).inventory.isEmpty()) {
				Minecraft.getMinecraft().playerController.windowClick(guiChest.inventorySlots.windowId, InventoryUtil.getClickSlot((int) slotToPick), 0, ClickType.QUICK_MOVE, Minecraft.getMinecraft().player);
				if (bypass.isChecked()) {
					mc.player.connection.sendPacket(new CPacketClickWindow(guiChest.inventorySlots.windowId, InventoryUtil.getClickSlot((int) slotToPick), 0, ClickType.QUICK_MOVE, guiChest.inventorySlots.getSlot((int) slotToPick).getStack(), (short) 1));
				}
				double emptySlot = InventoryUtil.getEmptySlot();
				Minecraft.getMinecraft().playerController.windowClick(mc.player.openContainer.windowId, InventoryUtil.getClickSlot((int) emptySlot), 0, ClickType.QUICK_MOVE, Minecraft.getMinecraft().player);
				if (bypass.isChecked()) {
					mc.player.connection.sendPacket(new CPacketClickWindow(guiChest.inventorySlots.windowId, InventoryUtil.getClickSlot((int) emptySlot), 0, ClickType.QUICK_MOVE, guiChest.inventorySlots.getSlot((int) emptySlot).getStack(), (short) 1));
				}
			}
		} else {
			slotToPick = 0;
		}
	}
}
