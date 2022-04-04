/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.KeyBindingUtils;
import net.wurstclient.forge.utils.MathUtils;
import net.wurstclient.forge.utils.TimerUtils;

import java.lang.reflect.Field;

public final class ElytraFlight extends Hack {

	private final EnumSetting<Mode> mode =
			new EnumSetting<>("Mode", "Boost or control?", Mode.values(), Mode.BOOST);

	private final SliderSetting upSpeed =
			new SliderSetting("UpSpeed", "Speed for going Up", 0.4, 0.1, 2, 0.005, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting baseSpeed =
			new SliderSetting("BaseSpeed", "Speed for going forwards, left, right and back", 0.4, 0.1, 2, 0.005, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting downSpeed =
			new SliderSetting("DownSpeed", "Speed for going down", 0.4, 0.1, 2, 0.005, SliderSetting.ValueDisplay.DECIMAL);

	private final CheckboxSetting fakelag =
			new CheckboxSetting("FakeLag", "Delays your players position",
					false);

	private final SliderSetting antiKick =
			new SliderSetting("AntiKick", "How much we telport down", 0.4, 0.1, 2, 0.005, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting antikickdelay =
			new SliderSetting("AntiKickDelay", "Delay for AntiKick", 500, 100, 1000, 100, SliderSetting.ValueDisplay.DECIMAL);

	private final CheckboxSetting vel =
			new CheckboxSetting("Velocity", "When jump and sneak are idle we keep you still in the air",
					false);

	private final SliderSetting pitch =
			new SliderSetting("Pitch", "Always maintain the same pitch, It i will be in packets", -90, -90, 90, 5, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting yaw =
			new SliderSetting("YawAddition", "Add to your yaw, it will be in packets", 0, -100, 100, 1, SliderSetting.ValueDisplay.DECIMAL);

	private final CheckboxSetting ncp =
			new CheckboxSetting("NCP", "Use this to help bypass NCP",
					false);

	private final CheckboxSetting timer =
			new CheckboxSetting("TimerTakeOff", "Slow down time when taking off",
					false);

	private final CheckboxSetting durwarning =
			new CheckboxSetting("DurabilityWarning", "Warns you if your elytra is going to break",
					false);

	private final CheckboxSetting alwaysdurwarning =
			new CheckboxSetting("DurabilityWarning [Always]", "Ever 2nd second we just let you know the durability anyway",
					false);

	private final CheckboxSetting nowater =
			new CheckboxSetting("IgnoreWater", "Ingores water velocity",
					false);

	private final SliderSetting timergen =
			new SliderSetting("TimerGeneral", "Timer speed when we are flying", 0.9, 0.1, 2, 0.01, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting pospoofx =
			new SliderSetting("PosSpoofX", "Send packets to add to your X position", 2, -2, 2, 0.01, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting pospoofy =
			new SliderSetting("PosSpoofY", "Send packets to be higher or lower than your position", 2, -2, 2, 0.01, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting pospoofz =
			new SliderSetting("PosSpoofZ", "Send packets to add to your Z position", 2, -2, 2, 0.01, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting swing =
			new SliderSetting("Swing", "For the limbs when flying", 0.9, 0.1, 2, 0.01, SliderSetting.ValueDisplay.DECIMAL);

	private final SliderSetting swingamount =
			new SliderSetting("SwingAmount", "Amount for the limbs when flying", 0.9, 0.1, 2, 0.01, SliderSetting.ValueDisplay.DECIMAL);

	private final CheckboxSetting takeoffhelper =
			new CheckboxSetting("TakeOffHelper", "Helps you take off",
					false);

	private final CheckboxSetting pilot =
			new CheckboxSetting("AutoPilot [ENABLE]", "Maintain autmaticaly a Y value and move forward",
					false);

	private final CheckboxSetting pilotlegit =
			new CheckboxSetting("AutoPilotLegitMode [ENABLE]", "Using keybinding utils to go up and down",
					false);

	private final SliderSetting piloty =
			new SliderSetting("PilotYValue", "The Y value we will aim for when autopilot is engaged", 150, 1, 360, 1, SliderSetting.ValueDisplay.DECIMAL);

	public ElytraFlight() {
		super("ElytraFlight", "Fly with elytras.");
		setCategory(Category.MOVEMENT);
		//These are the main settings
		addSetting(mode);
		addSetting(upSpeed);
		addSetting(baseSpeed);
		addSetting(downSpeed);
		addSetting(fakelag);
		addSetting(antiKick);
		addSetting(antikickdelay);
		//These are the misc settings
		addSetting(vel);
		addSetting(pitch);
		addSetting(yaw);
		addSetting(ncp);
		addSetting(timer);
		addSetting(durwarning);
		addSetting(alwaysdurwarning);
		addSetting(nowater);
		addSetting(timergen);
		addSetting(pospoofx);
		addSetting(pospoofy);
		addSetting(pospoofz);
		addSetting(swing);
		addSetting(swingamount);
		//This is for automation
		addSetting(takeoffhelper);
		addSetting(pilot);
		addSetting(pilotlegit);
		addSetting(piloty);
	}

	@Override
	protected void onEnable() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable() {
		MinecraftForge.EVENT_BUS.unregister(this);
		setTickLength(50);
	}

	@SubscribeEvent
	public void update(WUpdateEvent event) {
		if (mc.player.isElytraFlying()) {
			mc.player.limbSwing = swing.getValueF();
			mc.player.limbSwingAmount = swingamount.getValueF();

			if (TimerUtils.hasReached(antikickdelay.getValueI())) {
				TimerUtils.reset();
				mc.player.setPosition(mc.player.posX, mc.player.posY - antiKick.getValueF(), mc.player.posZ);
			}

			if (nowater.isChecked()) {
				nowater();
			}

			if (durwarning.isChecked() || alwaysdurwarning.isChecked()) {
				durability();
			}

			if (pilot.isChecked()) {
				autopilot();
			}

			if (takeoffhelper.isChecked()) {
				takeoffhelper();
			}

			if (timer.isChecked()) {
				timertakeoff();
			}

			if (ncp.isChecked()) {
				ncp();
			}

			if (mode.getSelected().control) {
				control();
			}

			if (mode.getSelected().boost) {
				boost();
			}
		}
	}

	private void nowater() {
		try {
			Field isInWater = Entity.class.getDeclaredField(
					wurst.isObfuscated() ? "field_70171_ac" : "inWater");
			isInWater.setAccessible(true);
			EntityPlayerSP player = mc.player;
			isInWater.setBoolean(player, false);
		} catch (ReflectiveOperationException e) {
			setEnabled(false);
			throw new RuntimeException(e);
		}
	}


	private void durability() {
		ItemStack dur = mc.player.inventory.armorInventory.get(2);
		boolean iselytra = dur.getItem().equals(Items.ELYTRA);
		double damage = dur.getItemDamage() - dur.getMaxDamage();

		if (alwaysdurwarning.isChecked() && !durwarning.isChecked()) {
			if (TimerUtils.hasReached(1000)) {
				ChatUtils.warning("The current durability of your elytra is:" + " " + damage);
			}
		}

		if (iselytra) {
			if (damage < 1) {
				mc.getSoundHandler().playSound(PositionedSoundRecord.getMusicRecord(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP));
				if (TimerUtils.hasReached(1000)) {
					ChatUtils.warning("REPLACE ELYTRA NOW");
				}
			} else {
				if (alwaysdurwarning.isChecked()) {
					if (TimerUtils.hasReached(1000)) {
						ChatUtils.warning("The current durability of your elytra is:" + " " + damage);
					}
				}
			}
		}
	}


	private void autopilot() {
		if (!pilotlegit.isChecked()) {
			if (mc.player.posY > piloty.getValueF()) {
				mc.player.setPosition(mc.player.posX, mc.player.posY - 0.1, mc.player.posZ);
			} else {
				mc.player.setPosition(mc.player.posX, mc.player.posY + 0.1, mc.player.posZ);
			}
		} else {
			if (mc.player.posY > piloty.getValueF()) {
				KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, true);
				KeyBindingUtils.setPressed(mc.gameSettings.keyBindJump, false);
			} else {
				KeyBindingUtils.setPressed(mc.gameSettings.keyBindSprint, false);
				KeyBindingUtils.setPressed(mc.gameSettings.keyBindJump, true);
			}
		}
	}

	private void takeoffhelper() {
		if (mc.player.onGround) {
			mc.player.jump();
		}
	}

	private void timertakeoff() {
		try {
			if (mc.player.fallDistance < 1) {
				setTickLength(50 / 0.5f);
			} else {
				setTickLength(50 / timergen.getValueF());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ncp() {
		try {
			mc.player.setPosition(mc.player.posX, mc.player.posY - 0.025, mc.player.posZ);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SubscribeEvent
	public void pitchyawposground(WPacketInputEvent event) {
		if (event.getPacket() instanceof CPacketPlayer.PositionRotation) {
			event.setCanceled(true);
			if (!fakelag.isChecked()) {
				mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + pospoofx.getValueF(), mc.player.posY + pospoofy.getValueF(), mc.player.posZ + pospoofz.getValueF(), mc.player.onGround));
				mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw.getValueF(), pitch.getValueF(), mc.player.onGround));
			} else {
				mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.prevPosX + pospoofx.getValueF(), mc.player.prevPosY + pospoofy.getValueF(), mc.player.prevPosZ + pospoofz.getValueF(), mc.player.onGround));
				mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw.getValueF(), pitch.getValueF(), mc.player.onGround));
			}
		}
	}

	private void control() {
		try {
			if (mc.player.moveForward != 0 || mc.player.moveStrafing != 0) {
				double[] dir = MathUtils.directionSpeed(baseSpeed.getValueF());
				mc.player.motionX = dir[0];
				mc.player.motionZ = dir[1];
			}

			if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
				if (vel.isChecked()) {
					mc.player.motionY = 0;
				}
			}

			if (mc.gameSettings.keyBindJump.isKeyDown()) {
				mc.player.motionY += upSpeed.getValueF();
			}

			if (mc.gameSettings.keyBindSneak.isKeyDown()) {
				mc.player.motionY -= downSpeed.getValueF();
			}

			if (vel.isChecked()) {
				if (mc.player.moveForward == 0 && mc.player.moveStrafing == 0 && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
					if (!ncp.isChecked()) {
						mc.player.setVelocity(0, 0, 0);
					} else {
						mc.player.motionY = 0;
						mc.player.setPosition(mc.player.posX, mc.player.posY - 0.04, mc.player.posZ);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void boost() {
		try {
			if (vel.isChecked()) {
				if (mc.player.moveForward == 0 && mc.player.moveStrafing == 0 && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
					if (!ncp.isChecked()) {
						mc.player.setVelocity(0, 0, 0);
					} else {
						mc.player.motionY = 0;
						mc.player.setPosition(mc.player.posX, mc.player.posY - 0.04, mc.player.posZ);
					}
				}
			}

			if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
				if (vel.isChecked()) {
					mc.player.motionY = 0;
				}
			}


			float yaw = Minecraft.getMinecraft().player.rotationYaw;
			float pitch = Minecraft.getMinecraft().player.rotationPitch;
			if (Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown()) {
				Minecraft.getMinecraft().player.motionX -= Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.05;
				Minecraft.getMinecraft().player.motionZ += Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)) * 0.05;
			}

			if (Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown())
				Minecraft.getMinecraft().player.motionY += upSpeed.getValueF();
			if (Minecraft.getMinecraft().gameSettings.keyBindSneak.isKeyDown())
				Minecraft.getMinecraft().player.motionY -= downSpeed.getValueF();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	private enum Mode {
		BOOST("Boost", true, false),
		CONTROL("Control", false, true);

		private final String name;
		private final boolean boost;
		private final boolean control;

		private Mode(String name, boolean boost, boolean control) {
			this.name = name;
			this.boost = boost;
			this.control = control;
		}

		public String toString() {
			return name;
		}

	}

	private void setTickLength(float tickLength) {
		try {
			Field fTimer = mc.getClass().getDeclaredField(
					wurst.isObfuscated() ? "field_71428_T" : "timer");
			fTimer.setAccessible(true);

			if (WMinecraft.VERSION.equals("1.10.2")) {
				Field fTimerSpeed = Timer.class.getDeclaredField(
						wurst.isObfuscated() ? "field_74278_d" : "timerSpeed");
				fTimerSpeed.setAccessible(true);
				fTimerSpeed.setFloat(fTimer.get(mc), 50 / tickLength);

			} else {
				Field fTickLength = Timer.class.getDeclaredField(
						wurst.isObfuscated() ? "field_194149_e" : "tickLength");
				fTickLength.setAccessible(true);
				fTickLength.setFloat(fTimer.get(mc), tickLength);
			}

		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}