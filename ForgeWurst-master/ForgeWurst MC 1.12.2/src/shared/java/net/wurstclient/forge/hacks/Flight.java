/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.utils.TimerUtils;

public final class Flight extends Hack {

	private final EnumSetting<Mode> mode =
			new EnumSetting<>("Mode", Mode.values(), Mode.NORMAL);

	public boolean shouldBlinkFly;
	public static double aimY;

	public static double starthealth;

	private enum Mode {
		NORMAL("Normal", true, false, false, false, false, false),
		JUMP("Jump", false, true, false, false, false, false),
		SPOOF("SpoofGround", false, false, true, false, false, false),
		NCPJump("NCPJump", false, false, false, true, false, false),
		BLINKFly("BlinkFly", false, false, false, false, true, false),
		BOPFly("BopFly", false, false, false, false, false, true);

		private final String name;
		private final boolean normal;
		private final boolean jump;
		private final boolean spoof;
		private final boolean ncpjump;
		private final boolean blinkfly;
		private final boolean bopfly;

		private Mode(String name, boolean normal, boolean jump, boolean spoof, boolean ncpjump, boolean blinkfly, boolean bopfly) {
			this.name = name;
			this.normal = normal;
			this.jump = jump;
			this.spoof = spoof;
			this.ncpjump = ncpjump;
			this.blinkfly = blinkfly;
			this.bopfly = bopfly;
		}

		public String toString() {
			return name;
		}
	}

	@Override
	public String getRenderName()
	{
		return getName() + " [" + mode.getSelected().name() + "]";
	}

	public Flight() {
		super("Flight", "I believe i can fly.");
		setCategory(Category.MOVEMENT);
		addSetting(mode);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
		TimerUtils.reset();
		starthealth = mc.player.getHealth();
		try {
			aimY = mc.player.posY;
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
		try {
			if (mode.getSelected().normal) {
				normalFly();
			}
			if (mode.getSelected().jump) {
				jumpFly();
			}
			if (mode.getSelected().spoof) {
				spoofFly();
			}
			if (mode.getSelected().ncpjump) {
				ncpJump();
			}
			if (mode.getSelected().blinkfly) {
				blinkFly();
			}
			if (mode.getSelected().bopfly) {
				bopFly();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void bopFly() {
		mc.player.onGround = true;
		mc.player.isAirBorne = false;
		mc.player.fallDistance = 0;
		mc.player.motionX = 0;
		mc.player.motionY = 0;
		mc.player.motionZ = 0;
		if (mc.player.ticksExisted % 2 == 0) {
			mc.player.setPosition(mc.player.posX, mc.player.posY + 0.15, mc.player.posZ);
		} else {
			mc.player.setPosition(mc.player.posX, mc.player.posY - 0.15, mc.player.posZ);
		}
		mc.player.jumpMovementFactor = 0.5F;
	}

	public void ncpJump() {
		try {
			mc.player.onGround = true;
			mc.player.fallDistance = 0;
			mc.player.isAirBorne = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void normalFly() {
		EntityPlayerSP player = mc.player;

		player.capabilities.isFlying = false;
		player.motionX = 0;
		player.motionY = 0;
		player.motionZ = 0;
		player.jumpMovementFactor = 1;

		if (mc.gameSettings.keyBindJump.isKeyDown())
			player.motionY += 1;
		if (mc.gameSettings.keyBindSneak.isKeyDown())
			player.motionY -= 1;
	}

	public void jumpFly() {
		try {
			if (mc.player.posY < aimY) {
				mc.player.jump();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void spoofFly() {
		try {
			EntityPlayerSP player = mc.player;

			player.capabilities.isFlying = false;
			player.motionX = 0;
			player.motionY = 0;
			player.motionZ = 0;
			player.jumpMovementFactor = 1;

			if (mc.gameSettings.keyBindJump.isKeyDown())
				player.motionY += 1;
			if (mc.gameSettings.keyBindSneak.isKeyDown())
				player.motionY -= 1;

			mc.player.setPosition(mc.player.posX, aimY + 0.5, mc.player.posZ);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void blinkFly() {
		try {
			if (!shouldBlinkFly)
				return;
			EntityPlayerSP player = mc.player;

			player.capabilities.isFlying = false;
			player.motionX = 0;
			player.motionY = 0;
			player.motionZ = 0;
			player.jumpMovementFactor = 1;

			if (mc.gameSettings.keyBindJump.isKeyDown())
				player.motionY += 1;
			if (mc.gameSettings.keyBindSneak.isKeyDown())
				player.motionY -= 1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void onComingPacket(WPacketInputEvent event) {
		try {
			if (mode.getSelected().spoof) {
				if (event.getPacket() instanceof CPacketPlayer.Rotation) {
					event.setCanceled(true);
					mc.player.connection.sendPacket(new CPacketPlayer.Rotation(mc.player.rotationYaw, mc.player.rotationPitch, true));
				}
				if (event.getPacket() instanceof CPacketPlayer.Position) {
					event.setCanceled(true);
					mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, true));
				}
				if (event.getPacket() instanceof CPacketPlayer.PositionRotation) {
					event.setCanceled(true);
					mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, mc.player.posY, mc.player.posZ, mc.player.rotationYaw, mc.player.rotationPitch, true));
				}
			}
			if (mode.getSelected().blinkfly) {
				if (mc.player.fallDistance > 0.75) {
					shouldBlinkFly = true;
					if (event.getPacket() instanceof CPacketPlayer && mc.player.ticksExisted % 2 == 0) {
						event.setCanceled(true);
					}
				} else {
					shouldBlinkFly = false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}