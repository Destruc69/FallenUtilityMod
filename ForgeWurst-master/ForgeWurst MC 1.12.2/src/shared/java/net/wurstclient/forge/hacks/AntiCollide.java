/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.renderer.debug.DebugRendererCollisionBox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class AntiCollide extends Hack {

	public AntiCollide() {
		super("AntiCollide", "Prevent colliding with players");
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
		for (Entity entity : mc.world.getLoadedEntityList()) {
			if (entity != mc.player) {
				if (entity instanceof EntityPlayer) {
					if (mc.player.getCollisionBox(entity) == mc.player.getCollisionBox(mc.player)) {
						mc.player.collidedHorizontally = false;
						mc.player.collidedVertically = false;
					}
				}
			}
		}
	}
}