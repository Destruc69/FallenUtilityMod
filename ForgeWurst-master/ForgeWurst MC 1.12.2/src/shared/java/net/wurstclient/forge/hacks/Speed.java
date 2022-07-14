/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.SliderSetting;

public final class Speed extends Hack {

	private final SliderSetting height =
			new SliderSetting("JumpHeight", "0.405 = normal jump", 0.405, 0.105, 0.705, 0.005, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting speed =
			new SliderSetting("Speed", "0.2 = normal speed", 0.2, 0.05, 0.4, 0.05, SliderSetting.ValueDisplay.DECIMAL);

	private final CheckboxSetting ncp =
			new CheckboxSetting("NCP-Strict", "Bypass NCP",
					true);

	public Speed() {
		super("Speed", "I Show Speed");
		setCategory(Category.MOVEMENT);
		addSetting(height);
		addSetting(speed);
		addSetting(ncp);
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
		if (!ncp.isChecked()) {
			if (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) {
				if (mc.player.onGround) {
					mc.player.motionY = height.getValueF();
					final float yaw = GetRotationYawForCalc();
					mc.player.motionX -= MathHelper.sin(yaw) * speed.getValueF();
					mc.player.motionZ += MathHelper.cos(yaw) * speed.getValueF();
				}
			}
		} else {
			if (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) {
				if (mc.player.onGround) {
					mc.player.motionY = 0.405f;
					final float yaw = GetRotationYawForCalc();
					mc.player.motionX -= MathHelper.sin(yaw) * 0.19;
					mc.player.motionZ += MathHelper.cos(yaw) * 0.19;
				}
			}
		}
	}

	private float GetRotationYawForCalc() {
		float rotationYaw = mc.player.rotationYaw;
		if (mc.player.moveForward < 0.0f) {
			rotationYaw += 180.0f;
		}
		float n = 1.0f;
		if (mc.player.moveForward < 0.0f) {
			n = -0.5f;
		} else if (mc.player.moveForward > 0.0f) {
			n = 0.5f;
		}
		if (mc.player.moveStrafing > 0.0f) {
			rotationYaw -= 90.0f * n;
		}
		if (mc.player.moveStrafing < 0.0f) {
			rotationYaw += 90.0f * n;
		}
		return rotationYaw * 0.017453292f;
	}
}