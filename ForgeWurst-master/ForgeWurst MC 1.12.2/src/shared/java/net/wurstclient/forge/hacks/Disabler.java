/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.network.play.client.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class Disabler extends Hack {

	public Disabler() {
		super("Disabler", "Bypass anti cheats.");
		setCategory(Category.PLAYER);
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
		abilitySpoof();
		spectatorSpoof();
		inputSpoof();
	}

	public void abilitySpoof() {
		mc.player.capabilities.allowFlying = true;

		mc.player.connection.sendPacket(new CPacketPlayerAbilities(mc.player.capabilities));
	}

	public void spectatorSpoof() {
		mc.player.connection.sendPacket(new CPacketSpectate(mc.player.getUniqueID()));
	}

	public void inputSpoof() {
		if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
			mc.player.connection.sendPacket(new CPacketInput(Float.MAX_VALUE, Float.MAX_VALUE, false, false));
		}
		if (mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
			mc.player.connection.sendPacket(new CPacketInput(Float.MAX_VALUE, Float.MAX_VALUE, true, false));
		}
		if (mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
			mc.player.connection.sendPacket(new CPacketInput(Float.MAX_VALUE, Float.MAX_VALUE, false, true));
		}
		if (mc.gameSettings.keyBindJump.isKeyDown() && mc.gameSettings.keyBindSneak.isKeyDown()) {
			mc.player.connection.sendPacket(new CPacketInput(Float.MAX_VALUE, Float.MAX_VALUE, true, true));
		}
	}

	@SubscribeEvent
	public void onPacket(WPacketInputEvent event) {
		if (event.getPacket() instanceof CPacketKeepAlive || event.getPacket() instanceof CPacketConfirmTransaction || event.getPacket() instanceof CPacketEntityAction || event.getPacket() instanceof CPacketCustomPayload) {
			event.setCanceled(true);
		}
	}
}